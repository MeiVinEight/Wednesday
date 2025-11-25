package org.mve.logging;

import kotlin.ExceptionsKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WednesdayLogger extends LegacyAbstractLogger implements Function3<SimpleLogger.LogPriority, String, Throwable, Unit>, MiraiLogger
{
	private static final Map<SimpleLogger.LogPriority, Consumer<Ansi>> PRIORITY_COLOR;
	public static final Map<SimpleLogger.LogPriority, String> PRIORITY_ALIGNED_NAME;
	public final SimpleLogger.LogPriority priority;
	private final List<Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit>> functions = new LinkedList<>();
	private boolean function = true;

	public WednesdayLogger(String name, SimpleLogger.LogPriority priority)
	{
		this.name = name;
		this.priority = priority;
	}

	public void function(Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function)
	{
		this.functions.add(function);
	}

	@Override
	public Unit invoke(SimpleLogger.LogPriority logPriority, String s, Throwable throwable)
	{
		if (logPriority == null)
			return null;
		if (this.priority.compareTo(logPriority) > 0)
			return null;
		if (s == null && throwable == null)
			return null;

		Ansi ansi = new Ansi().reset();
		ansi.a(timestamp())
			.a('[');

		if (PRIORITY_COLOR.containsKey(logPriority))
			PRIORITY_COLOR.get(logPriority).accept(ansi);
		ansi.a(PRIORITY_ALIGNED_NAME.get(logPriority))
			.reset()
			.a("] ");

		if(this.name != null)
			ansi.a('[')
				.a(this.name)
				.a("] ");

		if (s != null)
			ansi.a(s)
				.a('\n');

		if (throwable != null)
			ansi.a(ExceptionsKt.stackTraceToString(throwable));

		System.out.print(ansi);
		if (!function)
			return null;
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : this.functions)
		{
			try
			{
				function.invoke(this, logPriority, s, throwable);
			}
			catch (Throwable e)
			{
				this.function = false;
				this.error(e);
				this.function = true;
			}
		}
		return null;
	}

	@Override
	protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable)
	{
		if (messagePattern != null)
		{
			StringBuilder builder = new StringBuilder();
			boolean useArg = false;
			int argIdx = 0;
			for (int i = 0; i < messagePattern.length(); i++)
			{
				char c = messagePattern.charAt(i);
				if (c == '{')
				{
					if (useArg)
						builder.append('{');
					useArg = true;
				}
				else if (c == '}')
				{
					if (useArg && argIdx < arguments.length)
						builder.append(arguments[argIdx++]);
					else
						builder.append('}');
					useArg = false;
				}
				else
				{
					if (useArg)
					{
						builder.append('{');
						useArg = false;
					}
					builder.append(c);
				}
			}
			if (useArg)
				builder.append('{');
			messagePattern = builder.toString();
		}
		SimpleLogger.LogPriority priority = switch (level)
		{
			case TRACE -> SimpleLogger.LogPriority.VERBOSE;
			case DEBUG -> SimpleLogger.LogPriority.DEBUG;
			case INFO -> SimpleLogger.LogPriority.INFO;
			case WARN -> SimpleLogger.LogPriority.WARNING;
			case ERROR -> SimpleLogger.LogPriority.ERROR;
		};
		this.invoke(priority, messagePattern, throwable);
	}

	@Nullable
	@Override
	public String getIdentity()
	{
		return this.name;
	}

	@Override
	public void verbose(@Nullable String s)
	{
		this.trace(s);
	}

	@Override
	public void verbose(@Nullable String s, @Nullable Throwable throwable)
	{
		this.trace(s, throwable);
	}

	@Override
	public void warning(@Nullable String s)
	{
		this.warn(s);
	}

	@Override
	public void warning(@Nullable String s, @Nullable Throwable throwable)
	{
		this.warn(s, throwable);
	}

	@Override
	protected String getFullyQualifiedCallerName()
	{
		return this.name;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public boolean isTraceEnabled()
	{
		return true;
	}

	@Override
	public boolean isDebugEnabled()
	{
		return true;
	}

	@Override
	public boolean isInfoEnabled()
	{
		return true;
	}

	@Override
	public boolean isWarnEnabled()
	{
		return true;
	}

	@Override
	public boolean isErrorEnabled()
	{
		return true;
	}

	public static String timestamp()
	{
		LocalDateTime time = LocalDateTime.now();
		long ss = time.getSecond();
		long mm = time.getMinute();
		long hh = time.getHour();
		char[] buf = {
			'[',
			(char) ((hh / 10) + '0'),
			(char) ((hh % 10) + '0'),
			':',
			(char) ((mm / 10) + '0'),
			(char) ((mm % 10) + '0'),
			':',
			(char) ((ss / 10) + '0'),
			(char) ((ss % 10) + '0'),
			']',
			' '
		};
		return new String(buf);
	}

	static
	{
		PRIORITY_COLOR = new HashMap<>();
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.VERBOSE, ansi -> ansi.bold().fgBright(Ansi.Color.BLUE));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.DEBUG, ansi -> ansi.bold().fg(Ansi.Color.MAGENTA));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.INFO, ansi -> ansi.bold().fg(Ansi.Color.GREEN));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.WARNING, ansi -> ansi.bold().fg(Ansi.Color.YELLOW));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.ERROR, ansi -> ansi.bold().fg(Ansi.Color.RED));

		PRIORITY_ALIGNED_NAME = new HashMap<>();
		PRIORITY_ALIGNED_NAME.put(SimpleLogger.LogPriority.VERBOSE, "VERB");
		PRIORITY_ALIGNED_NAME.put(SimpleLogger.LogPriority.DEBUG, "DBUG");
		PRIORITY_ALIGNED_NAME.put(SimpleLogger.LogPriority.INFO, "INFO");
		PRIORITY_ALIGNED_NAME.put(SimpleLogger.LogPriority.WARNING, "WARN");
		PRIORITY_ALIGNED_NAME.put(SimpleLogger.LogPriority.ERROR, "ERRO");
	}
}
