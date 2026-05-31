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
	public static void main(String[] args)
	{
		new Main().accept(args);
	}

	@Override
	public void accept(String[] strings)
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
			// Wednesday wednesday = new Wednesday("ws://meivi.net:3001", "5uKbMvP");

			/*
			wednesday.subscribe.register("woden", new EchoingMessage(wednesday));
			Minecraft minecraft = new Minecraft();
			wednesday.subscribe.register("obf", minecraft::obfuscate);
			wednesday.subscribe.register("srg", minecraft::searge);
			wednesday.subscribe.register("mcp", minecraft::official);
			wednesday.subscribe.register(BotOfflineEvent.class, e -> wednesday.close());
			wednesday.subscribe.register(NudgeEvent.class, NudgeFacing::nudge);
			wednesday.subscribe.register(WrappedImage.class, NudgeFacing::capture);
			wednesday.subscribe.register(LightApp.class, ApplicationMessage::application);
			wednesday.subscribe.register(GroupMessageEvent.class, new RepeatingMessage(Configuration.REPEAT_PROBABILITY, Set.of(LightApp.class))::random);

			*/

			// wednesday.join();
			WednesdayWeb.main(strings);
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
		}
	}
}
