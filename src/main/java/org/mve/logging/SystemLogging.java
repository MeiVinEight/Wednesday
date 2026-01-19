package org.mve.logging;

import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.Configuration;

import java.util.ResourceBundle;

public class SystemLogging implements System.Logger
{
	private final WednesdayLogger logger;

	public SystemLogging(String name)
	{
		this.logger = LoggerManager.create(name);
	}

	@Override
	public String getName()
	{
		return this.logger.getName();
	}

	@Override
	public boolean isLoggable(Level level)
	{
		return mapping(level).ordinal() >= Configuration.COFFEE_SERVICE_LOG_LEVEL.getOrDefault(this.getName(), Configuration.LOG_LEVEL).ordinal();
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown)
	{
		this.logger.invoke(mapping(level), msg, thrown);
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String format, Object... params)
	{
		org.slf4j.event.Level level1 = switch (level)
		{
			case OFF, TRACE -> org.slf4j.event.Level.TRACE;
			case DEBUG -> org.slf4j.event.Level.DEBUG;
			case INFO -> org.slf4j.event.Level.INFO;
			case WARNING -> org.slf4j.event.Level.WARN;
			default -> org.slf4j.event.Level.ERROR;
		};
		this.logger.handleNormalizedLoggingCall(level1, null, format, params, null);
	}

	private static SimpleLogger.LogPriority mapping(Level level)
	{
		return switch (level)
		{
			case ALL, TRACE -> SimpleLogger.LogPriority.DEBUG;
			case DEBUG -> SimpleLogger.LogPriority.VERBOSE;
			case INFO -> SimpleLogger.LogPriority.INFO;
			case WARNING -> SimpleLogger.LogPriority.WARNING;
			default -> SimpleLogger.LogPriority.ERROR;
		};
	}
}
