package org.mve;

import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver;
import org.mve.logging.LoggerLazy;
import org.mve.logging.Coffee;
import org.mve.uni.Mirroring;
import org.mve.web.WebAPI;

import java.util.function.Consumer;

public class Main implements Consumer<String[]>
{
	public static void main(String[] args)
	{
		Coffee logger = new Coffee(null);
		System.setErr(System.out);
		logger.info("Wednesday 启动");


		Mirroring.set(
			ExternalResourceLeakObserver.class,
			"logger$delegate",
			new LoggerLazy()
		);

		try
		{
			Class.forName("org.sqlite.JDBC");
			WebAPI.main(args);
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
