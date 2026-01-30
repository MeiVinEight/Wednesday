package org.mve;

import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver;
import net.mamoe.mirai.message.data.LightApp;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerLazy;
import org.mve.logging.LoggerManager;
import org.mve.minecraft.Minecraft;
import org.mve.service.ApplicationMessage;
import org.mve.service.EchoingMessage;
import org.mve.service.NudgeFacing;
import org.mve.service.RepeatingMessage;
import org.mve.uni.Mirroring;
import top.mrxiaom.overflow.internal.message.data.WrappedImage;

import java.util.Set;

public class Main
{
	public static void main(String[] args)
	{
		LoggerManager.register(FileLogger.INSTANCE);
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP);


		// ExternalResourceLeakObserver.class.getDeclaredField("logger$delegate")
		Mirroring.set(
			ExternalResourceLeakObserver.class,
			"logger$delegate",
			new LoggerLazy()
		);

		try
		{
			Wednesday wednesday = new Wednesday();

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

			wednesday.join();
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
		}
	}
}
