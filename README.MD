<div align="center">
  <h1>Wednesday</h1>

  一个基于 Overflow 的 Java 机器人框架

  [![overflow](https://img.shields.io/badge/Overflow-1.0.8-blue?style=for-the-badge)](https://github.com/MrXiaoM/Overflow) [![mirai](https://img.shields.io/badge/mirai--core--api-2.16.0-blue?style=for-the-badge)](https://github.com/mamoe/mirai) [![Onebot 11](https://img.shields.io/badge/Onebot-11-313343?style=for-the-badge)](https://11.onebot.dev)
</div>

## 丨说明

> **注意: 本项目 <font color="#FF5555">不是</font> 一个开箱即用的工具，你需要至少部署一个可用的[OneBot11](https://onebot.dev/ecosystem)实现 (如 [LLOnebot](https://github.com/LLOneBot/LLOneBot)、[NapCat](https://github.com/NapNeko/NapCatQQ)、[Gensokyo](https://github.com/Hoshinonyaruko/Gensokyo) 等)**
> 
> **并正确开启一个 WebSocket 服务器，Orange 主动发起连接后才可正常使用**
> 
> 🚧该项功能目前积极开发中🚧
> 
> 🎨如果有其他想法欢迎在issues中提出🎨
> 
> ✨积极欢迎贡献代码✨

## 丨使用

1. 克隆项目到本地

   ```shell
   git clone https://github.com/MeiVinEight/Orange
   ```

2. 构建项目

   ```shell
   ./gradlew build
   ```

3. 导出依赖库

   ```shell
   ./gradlew depends
   ```
   
4. 使用Java运行

   ```shell
   java -cp "libs/core-1.0.0.jar" org.mve.core.Main
   ```

---

5. 或使用gradle运行

   ```shell
   ./gradlew run
   ```

## 丨配置
第一次运行会在运行目录生成一个配置文件`config.json`,这里给出每项的介绍
```json5
{
  // 可选字段
  // 日志记录等级，可选的配置项有 (DEBUG < VERBOSE < INFO < WARNING < ERROR)
  // 大小关系表示优先级
  // 只会记录优先级大于等于该配置项的日志
  "log-level": "INFO",
  // 可选字段
  // WebUI监听的IP地址
  // 127.0.0.1: 仅允许本地访问
  // 0.0.0.0: 允许所有来源
  "address": "127.0.0.1",
  // 可选字段
  // WebUI绑定的端口
  "port": 8000
}
```

## 丨注意

+ 首次启动会自动创建上述配置文件，也可以在启动之前手动创建。
+ 首次启动会创建初始WebAPI登陆密码并输出在日志中，请记录输出的密码。
+ 若忘记密码可删除`data/WEBUI.DAT`文件，下次启动会重新生成登录密码。

## 丨其他

+ 本项目仅供学习使用，请勿用于非法用途
+ **[MIT](https://github.com/MeiVinEight/Wednesday/blob/master/LICENSE)**