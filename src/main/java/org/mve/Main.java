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


		//ExternalResourceLeakObserver.class.getDeclaredField("logger$delegate")
		Mirroring.set(
			ExternalResourceLeakObserver.class,
			"logger$delegate",
			new LoggerLazy()
		);

		try
		{
			Class.forName("org.sqlite.JDBC");
			Class.forName("org.mve.service.NudgeFacing");
			//Wednesday.SUBSCRIBE.register(NudgeEvent.class, NudgeFacing::nudge);
			//Wednesday.SUBSCRIBE.register(WrappedImage.class, NudgeFacing::capture);

			/*
			wednesday.SUBSCRIBE.register("woden", new EchoingMessage(wednesday));
			Minecraft minecraft = new Minecraft();
			Wednesday.SUBSCRIBE.register("obf", minecraft::obfuscate);
			Wednesday.SUBSCRIBE.register("srg", minecraft::searge);
			Wednesday.SUBSCRIBE.register("mcp", minecraft::official);
			Wednesday.SUBSCRIBE.register(LightApp.class, ApplicationMessage::application);
			Wednesday.SUBSCRIBE.register(GroupMessageEvent.class, new RepeatingMessage(Configuration.REPEAT_PROBABILITY, Set.of(LightApp.class))::random);

			*/

			// wednesday.join();
			WednesdayWeb.main(args);
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
			Wednesday.SYNCHRONIZE.close();
		}
	}
}
