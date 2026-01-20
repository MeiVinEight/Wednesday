package org.mve.logging;

import kotlin.ExceptionsKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;
import net.mamoe.mirai.utils.SimpleLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit>
{
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
	private static final WednesdayLogger LOGGER = LoggerManager.create();
	public static final FileLogger INSTANCE = new FileLogger();
	private PrintStream out;

	public FileLogger()
	{
		this.startup();
	}

	@Override
	public Unit invoke(WednesdayLogger logger, SimpleLogger.LogPriority priority, String s, Throwable throwable)
	{
		if (priority == null)
			return null;
		if (logger.priority.compareTo(priority) > 0)
			return null;
		if (this.out == null)
			return null;

		StringBuilder builder = new StringBuilder()
			.append(WednesdayLogger.timestamp())
			.append('[')
			.append(WednesdayLogger.PRIORITY_ALIGNED_NAME.get(priority))
			.append("] ");
		if (logger.getName() != null)
			builder
				.append('[')
				.append(logger.getName())
				.append("] ");
		if (s != null)
			builder.append(s).append('\n');
		if (throwable != null)
			builder.append(ExceptionsKt.stackTraceToString(throwable));
		this.out.print(builder);
		this.out.flush();
		return null;
	}

	public void startup()
	{
		if (this.out != null)
			this.out.close();
		LocalDateTime now = LocalDateTime.now();
		File logFile = new File("logs/" + DATE_TIME_FORMAT.format(now) + ".log");
		boolean ignored = logFile.getParentFile().mkdirs();
		try
		{
			this.out = new PrintStream(new FileOutputStream(logFile));
		}
		catch (FileNotFoundException e)
		{
			LOGGER.error(e);
		}
	}

	public void close()
	{
		if (this.out != null)
			this.out.close();
	}
}
