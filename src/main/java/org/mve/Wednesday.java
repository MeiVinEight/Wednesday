package org.mve;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Mirroring;
import top.mrxiaom.overflow.BotBuilder;
import top.mrxiaom.overflow.OverflowAPI;

import java.util.Objects;

public class Wednesday extends Synchronize
{
	public static final String SID = "Wednesday";
	public final SynchronizeNET synchronize;
	public static final WednesdayLogger LOGGER = LoggerManager.create(SID, Configuration.LOG_LEVEL);
	public final Bot QQ;
	public final SubscribeMessage subscribe;

	public Wednesday()
	{
		this.QQ = BotBuilder.positive(Configuration.ONEBOT_WS_FORWARD)
			.token(Configuration.ONEBOT_TOKEN)
			.modifyBotConfiguration(config ->
			{
				config.setBotLoggerSupplier(bot -> LOGGER);
				config.setNetworkLoggerSupplier(bot -> LoggerManager.create("Network", Configuration.LOG_LEVEL));
				config.setReconnectionRetryTimes(2);
			})
			.retryTimes(Configuration.ONEBOT_RECONNECT)
			.retryRestMills(-1)
			.overrideLogger(LOGGER)
			.connect();
		Objects.requireNonNull(QQ);
		this.synchronize = new SynchronizeNET();
		this.synchronize.offer(this);
		this.subscribe = new SubscribeMessage(this);
		this.synchronize.offer(this.subscribe);
	}

	public void close()
	{
		LOGGER.info(LoggerMessage.LOG_WEDNESDAY_SHUTDOWN);
		this.cancel();
		this.synchronize.close();
		this.subscribe.cancel();
		if (this.QQ != null)
			this.QQ.close();
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
			Mirroring.set(OverflowAPI.Companion.class, "logger", LoggerManager.create("OverflowAPI", SimpleLogger.LogPriority.DEBUG));
		}
		catch (Throwable t)
		{
			LOGGER.error("", t);
		}
	}
}
