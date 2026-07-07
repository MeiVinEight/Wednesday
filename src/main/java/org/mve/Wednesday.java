package org.mve;

import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;

public class Wednesday
{
	public static final String SID = "Wednesday";
	public static final SynchronizeNET SYNCHRONIZE = new SynchronizeNET();
	public static final SubscribeMessage SUBSCRIBE = new SubscribeMessage();
	public static final WednesdayLogger LOGGER = LoggerManager.create(SID);

	public Wednesday(String url, String token)
	{
	}

	public void close()
	{
		LOGGER.info(LoggerMessage.LOG_WEDNESDAY_SHUTDOWN);
	}

	public void exception(Throwable t)
	{
		LOGGER.error(null, t);
		//this.exception = t;
	}

	static
	{
	}
}
