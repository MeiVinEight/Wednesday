package org.mve;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import top.mrxiaom.overflow.BotBuilder;
import top.mrxiaom.overflow.OverflowAPI;

import java.lang.invoke.MethodHandle;

public class Wednesday extends Synchronize
{
	public static final String SID = "Wednesday";
	public final SynchronizeNET synchronize = new SynchronizeNET();
	public static final WednesdayLogger LOGGER = new WednesdayLogger(SID, Configuration.LOG_LEVEL);
	private final Bot QQ;

	public Wednesday()
	{
		this.synchronize.offer(this);
		new Thread(this.synchronize).start();
		this.QQ = BotBuilder.positive(Configuration.ONEBOT_WS_FORWARD)
			.token(Configuration.ONEBOT_TOKEN)
			.modifyBotConfiguration(config ->
			{
				config.setBotLoggerSupplier(bot -> LOGGER);
				config.setNetworkLoggerSupplier(bot -> new WednesdayLogger("Network", Configuration.LOG_LEVEL));
				config.setReconnectionRetryTimes(2);
			})
			.retryTimes(0)
			.retryRestMills(-1)
			.overrideLogger(LOGGER)
			.connect();
		if (QQ == null)
		{
			this.close();
			return;
		}
		SubscribeMessage sub = new SubscribeMessage(this);
		QQ.getEventChannel().subscribe(MessageEvent.class, sub);
		this.synchronize.offer(sub);
		QQ.getEventChannel().subscribeAlways(BotOfflineEvent.class, event -> Wednesday.this.close());
	}

	public void close()
	{
		LOGGER.info(LoggerMessage.LOG_WEDNESDAY_SHUTDOWN);
		this.cancel();
		this.synchronize.close();
		if (this.QQ != null)
			this.QQ.close();
	}

	@Override
	public void run()
	{
	}

	static
	{
		try
		{
			MethodHandle logger = ModuleAccess.LOOKUP.findStaticSetter(OverflowAPI.Companion.class, "logger", MiraiLogger.class);
			logger.invokeExact((MiraiLogger) new WednesdayLogger("OverflowAPI", SimpleLogger.LogPriority.DEBUG));
		}
		catch (Throwable t)
		{
			LOGGER.error("", t);
		}
	}
}
