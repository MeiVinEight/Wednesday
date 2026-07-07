package org.mve;

import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerLazy;
import org.mve.logging.LoggerManager;
import org.mve.uni.Mirroring;
import org.mve.web.WednesdayWeb;

import java.util.function.Consumer;

public class Main implements Consumer<String[]>
{
	public static void main(String[] args) throws Throwable
	{
		System.setErr(System.out);
		LoggerManager.register(FileLogger.INSTANCE);
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP);


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
			Wednesday.LOGGER.error(e);
			Wednesday.SYNCHRONIZE.close();
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
