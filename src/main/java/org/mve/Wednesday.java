package org.mve;

import cn.evolvefield.onebot.client.config.BotConfig;
import cn.evolvefield.onebot.client.connection.ConnectFactory;
import cn.evolvefield.onebot.client.connection.PositiveOneBotProducer;
import cn.evolvefield.onebot.client.connection.WSClient;
import cn.evolvefield.onebot.client.core.Bot;
import com.google.gson.JsonObject;
import kotlin.Result;
import kotlin.jvm.functions.Function1;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.SimpleLogger;
import org.java_websocket.framing.CloseFrame;
import org.mve.coroutine.ContinuationWednesday;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Mirroring;
import top.mrxiaom.overflow.OverflowAPI;
import top.mrxiaom.overflow.internal.Overflow;
import top.mrxiaom.overflow.internal.contact.BotWrapper;

import java.io.File;
import java.util.Objects;

public class Wednesday
{
	public static final String SID = "Wednesday";
	public static final SynchronizeNET SYNCHRONIZE = new SynchronizeNET();
	public static final SubscribeMessage SUBSCRIBE = new SubscribeMessage();
	public static final WednesdayLogger LOGGER = LoggerManager.create(SID, Configuration.LOG_LEVEL);
	public final BotWrapper QQ;

	public Wednesday(String url, String token)
	{
		/*
		this.QQ = (BotWrapper) Objects.requireNonNull(BotBuilder.positive(url)
			.token(token)
			.modifyBotConfiguration(config ->
			{
				config.setBotLoggerSupplier(bot -> LOGGER);
				config.setNetworkLoggerSupplier(bot -> LoggerManager.create("Network", Configuration.LOG_LEVEL));
				config.setReconnectionRetryTimes(0);
			})
			.retryTimes(0)
			.retryRestMills(-1)
			.overrideLogger(LOGGER)
			.connect());
		 */

		LOGGER.info("Overflow v{} 正在运行", Overflow.version);
		LOGGER.info("连接到 WebSocket: {}", url);
		OverflowAPI.get();
		Throwable[] ref = new Throwable[1];
		Bot bot0 = null;
		try
		{
			BotConfiguration configuratin = new BotConfiguration();
			configuratin.setBotLoggerSupplier(bot -> LoggerManager.create("BOT", Configuration.LOG_LEVEL));
			configuratin.setNetworkLoggerSupplier(bot -> LoggerManager.create("NETWORK", Configuration.LOG_LEVEL));
			configuratin.setReconnectionRetryTimes(0);
			BotConfig config = new BotConfig(
				url, // URL
				-1, // reversedPort
				token,
				!token.isEmpty(), // isAccessToken
				false, // noPlatform
				false, // useCQCode,
				0, // retryTimes
				-1, // retryWaitMills
				60_000L, // retryRestMills
				60, // heartbeatCheckSeconds
				false, // useGroupUploadEventForFileMessage
				false, // dropEventsBeforeConnected
				null // parentJob
			);
			ConnectFactory factory = ConnectFactory.create(config, config.getParentJob(), LoggerManager.create("Connect", Configuration.LOG_LEVEL));
			PositiveOneBotProducer service = (PositiveOneBotProducer) factory.createProducer();

			WSClient client = Mirroring.get(service.getClass(), "client", service);
			client.setOnError(t -> ref[0] = t);

			ContinuationWednesday<?> future = new ContinuationWednesday<>();
			Object suspendRet = client.connectSuspend(null);
			//client.run();
			boolean result = Boolean.TRUE.equals(Mirroring.checkcast(suspendRet));
			if (ref[0] != null)
				Mirroring.thrown(ref[0]);
			if (!result)
				throw new UnknownError("Unknown error causing connect failure");


			bot0 = (Bot) client.createBot(Mirroring.checkcast(future));
			Objects.requireNonNull(bot0);
			future = new ContinuationWednesday<>();

			LOGGER.info("正在请求 Onebot 版本信息");
			JsonObject versionInfo = bot0.getVersionInfo();
			LOGGER.info("协议端版本信息\n{}", versionInfo.getAsJsonObject("data"));
			if (bot0.getOnebotVersion() == 12)
				throw new IllegalStateException("Overflow 暂不支持 Ontbot 12");
			Function1<Long, File> workingDir = (val) -> new File("data", val.toString());
			BotWrapper.Companion.wrap$overflow_core(bot0, configuratin, workingDir, Mirroring.checkcast(future));
			Object wrapResult = future.get();
			if (wrapResult instanceof BotWrapper botw)
			{
				this.QQ = botw;
			}
			else if (wrapResult instanceof Result.Failure failure)
			{
				throw failure.exception;
			}
			else
				throw new Exception("Unknown error casuing connect failure " + wrapResult);
			bot0 = this.QQ.getImpl();
			future = new ContinuationWednesday<>(this.QQ.getCoroutineContext());
			this.QQ.getEventDispatcher().broadcast(new BotOnlineEvent(this.QQ), Mirroring.checkcast(future));
		}
		catch (Throwable e)
		{
			if (bot0 != null)
				bot0.getChannel().close(CloseFrame.NORMAL, "连接失败");
			if (ref[0] != null && e != ref[0])
				e.initCause(ref[0]);
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
		this.QQ.getEventChannel().subscribe(Event.class, SUBSCRIBE);
	}

	public void close()
	{
		LOGGER.info(LoggerMessage.LOG_WEDNESDAY_SHUTDOWN);
		this.QQ.close();
	}

	static
	{
		try
		{
			Mirroring.set(OverflowAPI.Companion.class, "logger", LoggerManager.create("OverflowAPI", SimpleLogger.LogPriority.DEBUG));
		}
		catch (Throwable t)
		{
			LOGGER.error("", t);
		}
		SYNCHRONIZE.offer(SUBSCRIBE);
	}
}
