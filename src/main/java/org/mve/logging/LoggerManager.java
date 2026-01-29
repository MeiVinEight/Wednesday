package org.mve.logging;

import kotlin.Unit;
import kotlin.jvm.functions.Function4;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.Configuration;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoggerManager implements ILoggerFactory
{
	private static final List<Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit>> FUNCTION = new ArrayList<>();

	public static void register(Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function)
	{
		LoggerManager.FUNCTION.add(function);
	}

	public static WednesdayLogger create()
	{
		WednesdayLogger logger = new WednesdayLogger(null, SimpleLogger.LogPriority.DEBUG);
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : LoggerManager.FUNCTION)
			logger.function(function);
		return logger;
	}

	public static WednesdayLogger create(String name)
	{
		WednesdayLogger logger = new WednesdayLogger(name, SimpleLogger.LogPriority.DEBUG);
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : LoggerManager.FUNCTION)
			logger.function(function);
		return logger;
	}

	public static WednesdayLogger create(String name, SimpleLogger.LogPriority priority)
	{
		WednesdayLogger logger = new WednesdayLogger(name, priority);
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : LoggerManager.FUNCTION)
			logger.function(function);
		return logger;
	}

	@Override
	public Logger getLogger(String name)
	{
		return create(name, Configuration.LOG_LEVEL);
	}
}
