package org.mve;

import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerManager;
import org.mve.minecraft.Minecraft;
import org.mve.service.EchoingMessage;
import org.mve.service.NudgeFacing;
import org.mve.uni.Mirroring;

public class Main
{
	public static void main(String[] args)
	{
		LoggerManager.register(FileLogger.INSTANCE);
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP);
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP_PATCHING);

		// Patcher
		// Make LogPriority.DEBUG.ordinal() < LogPriority.VERBOSE.ordinal()
		{
			try
			{
				String ordinalFieldName = "ordinal";
				int verbOrdinal = Mirroring.get(Enum.class, ordinalFieldName, SimpleLogger.LogPriority.VERBOSE);
				int dbugOrdinal = Mirroring.get(Enum.class, ordinalFieldName, SimpleLogger.LogPriority.DEBUG);
				Mirroring.set(Enum.class, ordinalFieldName, SimpleLogger.LogPriority.DEBUG, verbOrdinal);
				Mirroring.set(Enum.class, ordinalFieldName, SimpleLogger.LogPriority.VERBOSE, dbugOrdinal);
				Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC);
			}
			catch (Throwable e)
			{
				Wednesday.LOGGER.warn(LoggerMessage.LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL, e);
			}
		}

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
			wednesday.join();
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
		}
	}
}
