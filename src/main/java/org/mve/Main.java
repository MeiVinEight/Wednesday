package org.mve;

import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerLazy;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Mirroring;
import org.mve.web.WednesdayWeb;

import java.util.function.Consumer;

public class Main implements Consumer<String[]>
{
	public static void main(String[] args) throws Throwable
	{
		WednesdayLogger logger = new WednesdayLogger(null);
		System.setErr(System.out);
		LoggerManager.register(FileLogger.INSTANCE);
		logger.info(LoggerMessage.LOG_WEDNESDAY_STARTUP);


		Mirroring.set(
			ExternalResourceLeakObserver.class,
			"logger$delegate",
			new LoggerLazy()
		);

		try
		{
			Class.forName("org.sqlite.JDBC");
			WednesdayWeb.main(args);
		}
		catch (Throwable e)
		{
			logger.error(e);
		}
	}

	@Override
	public void accept(String[] strings)
	{
		try
		{
			main(strings);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
		}
	}
}
