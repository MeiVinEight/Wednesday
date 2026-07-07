import { Input } from '@heroui/input';
import { useRequest } from 'ahooks';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import toast from 'react-hot-toast';

import SaveButtons from '@/components/button/save_buttons';
import PageLoading from '@/components/page_loading';

import WebUIManager from '@/controllers/webui_manager';

const ServerConfigCard = () => {
  const {
    data: configData,
    loading: configLoading,
    error: configError,
    refreshAsync: refreshConfig,
  } = useRequest(WebUIManager.getWebUIConfig);

  const [ipListText, setIpListText] = useState('');

  const {
    control,
    handleSubmit: handleConfigSubmit,
    formState: { isSubmitting },
    setValue: setConfigValue,
    watch,
  } = useForm<{
    loglevel: string;
    host: string;
    port: number;
    loginRate: number;
    disableWebUI: boolean;
    accessControlMode: 'none' | 'whitelist' | 'blacklist';
    ipWhitelist: string[];
    ipBlacklist: string[];
    enableXForwardedFor: boolean;
  }>({
    defaultValues: {
      loglevel: 'INFO',
      host: '127.0.0.1',
      port: 8000,
      loginRate: 10,
      disableWebUI: false,
      accessControlMode: 'none',
      ipWhitelist: [],
      ipBlacklist: [],
      enableXForwardedFor: false,
    },
  });

  const accessControlMode = watch('accessControlMode');

  const reset = () => {
    if (configData) {
      setConfigValue('host', configData.host);
      setConfigValue('port', configData.port);
      setConfigValue('loginRate', configData.loginRate);
      setConfigValue('disableWebUI', configData.disableWebUI);
      setConfigValue('accessControlMode', configData.accessControlMode || 'none');
      setConfigValue('ipWhitelist', configData.ipWhitelist || []);
      setConfigValue('ipBlacklist', configData.ipBlacklist || []);
      setConfigValue('enableXForwardedFor', configData.enableXForwardedFor || false);
      setConfigValue('loglevel', configData.loglevel || '')

      // 更新IP列表文本
      if (configData.accessControlMode === 'whitelist') {
        setIpListText((configData.ipWhitelist || []).join('\n'));
      } else if (configData.accessControlMode === 'blacklist') {
        setIpListText((configData.ipBlacklist || []).join('\n'));
      }
    }
  };

  const onSubmit = handleConfigSubmit(async (data) => {
    try {
      // 解析IP列表
      const ipList = ipListText
        .split('\n')
        .map(ip => ip.trim())
        .filter(ip => ip.length > 0);

      const submitData = {
        ...data,
        ipWhitelist: data.accessControlMode === 'whitelist' ? ipList : [],
        ipBlacklist: data.accessControlMode === 'blacklist' ? ipList : [],
      };

      await WebUIManager.updateWebUIConfig(submitData);
      toast.success('保存成功');
    } catch (error) {
      const msg = (error as Error).message;
      toast.error(`保存失败: ${msg}`);
    }
  });

  const onRefresh = async () => {
    try {
      await refreshConfig();
      toast.success('刷新成功');
    } catch (error) {
      const msg = (error as Error).message;
      toast.error(`刷新失败: ${msg}`);
    }
  };

  useEffect(() => {
    reset();
  }, [configData]);

  useEffect(() => {
    // 当模式切换时，更新IP列表文本
    const handleModeChange = async () => {
      if (configData) {
        if (accessControlMode === 'whitelist') {
          const currentList = configData.ipWhitelist || [];
          // 如果白名单为空，自动获取当前IP并填入
          if (currentList.length === 0) {
            try {
              const clientIPData = await WebUIManager.getClientIP();
              if (clientIPData?.ip) {
                setIpListText(clientIPData.ip);
              } else {
                setIpListText('');
              }
            } catch (error) {
              console.error('获取客户端IP失败:', error);
              setIpListText('');
            }
          } else {
            setIpListText(currentList.join('\n'));
          }
        } else if (accessControlMode === 'blacklist') {
          setIpListText((configData.ipBlacklist || []).join('\n'));
        } else {
          setIpListText('');
        }
      }
    };

    handleModeChange();
  }, [accessControlMode, configData]);

  if (configLoading) return <PageLoading loading />;

  return (
    <>
      <title>服务器配置 - NapCat WebUI</title>
      <div className='flex flex-col gap-4'>
        <div className='flex flex-col gap-3'>
          <div className='flex-shrink-0 w-full font-bold text-default-600 dark:text-default-400 px-1'>服务器配置</div>
            <Controller
                control={control}
                name='loglevel'
                render={({ field }) => (
                    <Input
                        {...field}
                        label='日志等级'
                        placeholder='请输入日志等级'
                        description='服务器日志等级，从低到高分别为 DEBUG < VERBOSE < INFO < WARNING < ERROR'
                        isDisabled={!!configError}
                        errorMessage={configError ? '获取配置失败' : undefined}
                        classNames={{
                            inputWrapper:
                                'bg-default-100/50 dark:bg-white/5 backdrop-blur-md border border-transparent hover:bg-default-200/50 dark:hover:bg-white/10 transition-all shadow-sm data-[hover=true]:border-default-300',
                            input: 'bg-transparent text-default-700 placeholder:text-default-400',
                        }}
                    />
                )}
            />
          <Controller
            control={control}
            name='host'
            render={({ field }) => (
              <Input
                {...field}
                label='监听地址'
                placeholder='请输入监听地址'
                description='服务器监听的IP地址，0.0.0.0表示监听所有网卡'
                isDisabled={!!configError}
                errorMessage={configError ? '获取配置失败' : undefined}
                classNames={{
                  inputWrapper:
                    'bg-default-100/50 dark:bg-white/5 backdrop-blur-md border border-transparent hover:bg-default-200/50 dark:hover:bg-white/10 transition-all shadow-sm data-[hover=true]:border-default-300',
                  input: 'bg-transparent text-default-700 placeholder:text-default-400',
                }}
              />
            )}
          />
          <Controller
            control={control}
            name='port'
            render={({ field }) => (
              <Input
                {...field}
                type='number'
                value={field.value?.toString() || ''}
                label='监听端口'
                placeholder='请输入监听端口'
                description='服务器监听的端口号，范围1-65535'
                isDisabled={!!configError}
                errorMessage={configError ? '获取配置失败' : undefined}
                onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                classNames={{
                  inputWrapper:
                    'bg-default-100/50 dark:bg-white/5 backdrop-blur-md border border-transparent hover:bg-default-200/50 dark:hover:bg-white/10 transition-all shadow-sm data-[hover=true]:border-default-300',
                  input: 'bg-transparent text-gray-800 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500',
                }}
              />
            )}
          />
        </div>
      </div>

      <SaveButtons
        onSubmit={onSubmit}
        reset={reset}
        isSubmitting={isSubmitting || configLoading}
        refresh={onRefresh}
      />
    </>
  );
};

export default ServerConfigCard;
