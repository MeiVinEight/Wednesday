package org.mve;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.mc.Minecraft;
import top.mrxiaom.overflow.BotBuilder;
import top.mrxiaom.overflow.OverflowAPI;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.concurrent.CancellationException;

public class Wednesday extends Synchronize
{
	public static final String SID = "Wednesday";
	public final SynchronizeNET synchronize = new SynchronizeNET();
	public static final WednesdayLogger LOGGER = LoggerManager.create(SID, Configuration.LOG_LEVEL);
	private final Bot QQ;
	private final Listener<Event> subscribe;

	public Wednesday()
	{
		this.synchronize.offer(this);
		this.QQ = BotBuilder.positive(Configuration.ONEBOT_WS_FORWARD)
			.token(Configuration.ONEBOT_TOKEN)
			.modifyBotConfiguration(config ->
			{
				config.setBotLoggerSupplier(bot -> LOGGER);
				config.setNetworkLoggerSupplier(bot -> LoggerManager.create("Network", Configuration.LOG_LEVEL));
				config.setReconnectionRetryTimes(2);
			})
			.retryTimes(0)
			.retryRestMills(-1)
			.overrideLogger(LOGGER)
			.connect();
		Objects.requireNonNull(QQ);
		SubscribeMessage sub = new SubscribeMessage(this);
		sub.register("woden", new EchoingMessage(this));
		Minecraft minecraft = new Minecraft();
		sub.register("obf", minecraft::obfuscate);
		sub.register("srg", minecraft::searge);
		sub.register("mcp", minecraft::official);
		this.subscribe = QQ.getEventChannel().subscribe(Event.class, sub);
		this.synchronize.offer(sub);
	}

	public void close()
	{
		LOGGER.info(LoggerMessage.LOG_WEDNESDAY_SHUTDOWN);
		this.cancel();
		this.synchronize.close();
		if (this.subscribe != null)
			this.subscribe.cancel(new CancellationException("close"));
		if (this.QQ != null)
			this.QQ.close();
		FileLogger.INSTANCE.close();
	}

	public void join()
	{
		if (this.QQ != null)
			this.QQ.join();
		while (this.synchronize.thread.isAlive())
		{
			try
			{
				this.synchronize.thread.join();
			}
			catch (InterruptedException ignored)
			{
			}
		}
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
			logger.invokeExact((MiraiLogger) LoggerManager.create("OverflowAPI", SimpleLogger.LogPriority.DEBUG));
		}
		catch (Throwable t)
		{
			LOGGER.error("", t);
		}
	}
}
