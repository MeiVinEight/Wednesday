package org.mve;

import net.mamoe.mirai.utils.SimpleLogger;

import java.lang.invoke.MethodHandle;

public class Main
{
	public static void main(String[] args)
	{
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP);
		Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_STARTUP_PATCHING);

		// Patcher
		// Make LogPriority.DEBUG.ordinal() < LogPriority.VERBOSE.ordinal()
		{
			try
			{
				MethodHandle getOrdinal = ModuleAccess.LOOKUP.findGetter(Enum.class, "ordinal", int.class);
				MethodHandle setOrdinal = ModuleAccess.LOOKUP.findSetter(Enum.class, "ordinal", int.class);
				int verbOrdinal = (int) getOrdinal.invokeExact((Enum<SimpleLogger.LogPriority>) SimpleLogger.LogPriority.VERBOSE);
				int dbugOrdinal = (int) getOrdinal.invokeExact((Enum<SimpleLogger.LogPriority>) SimpleLogger.LogPriority.DEBUG);
				setOrdinal.invoke((Enum<SimpleLogger.LogPriority>) SimpleLogger.LogPriority.VERBOSE, dbugOrdinal);
				setOrdinal.invoke((Enum<SimpleLogger.LogPriority>) SimpleLogger.LogPriority.DEBUG, verbOrdinal);
				Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC);
			}
			catch (Throwable e)
			{
				Wednesday.LOGGER.info(LoggerMessage.LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL);
			}
		}

		Wednesday wednesday = new Wednesday();
		Runtime.getRuntime().addShutdownHook(wednesday.shutdown);
		wednesday.join();
	}
}
