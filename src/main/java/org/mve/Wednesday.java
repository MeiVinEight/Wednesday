package org.mve;

import net.mamoe.mirai.Bot;
import top.mrxiaom.overflow.BotBuilder;

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
}
