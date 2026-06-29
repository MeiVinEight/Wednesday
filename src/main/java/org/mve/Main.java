package org.mve;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.BotConfiguration;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerLazy;
import org.mve.logging.LoggerManager;
import org.mve.sn.core.Supernova;
import org.mve.sn.event.SupernovaManager;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.app.ILightApp;
import org.mve.sn.message.app.MessageLightApp;
import org.mve.uni.CompletionWaiting;
import org.mve.uni.Mirroring;

import java.util.concurrent.TimeUnit;
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
			//WednesdayWeb.main(strings);
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
			Wednesday.SYNCHRONIZE.close();
		}
	}
}
