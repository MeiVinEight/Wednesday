package org.mve;

import kotlin.ExceptionsKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import net.mamoe.mirai.utils.SimpleLogger;
import org.fusesource.jansi.Ansi;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class WednesdayLogger implements Function3<SimpleLogger.LogPriority, String, Throwable, Unit>
{
	private static final Map<SimpleLogger.LogPriority, Consumer<Ansi>> PRIORITY_COLOR;

	@Override
	public Unit invoke(SimpleLogger.LogPriority logPriority, String s, Throwable throwable)
	{
		if (s == null && throwable == null)
			return null;
		Ansi ansi = new Ansi();
		ansi.a(Ansi.Attribute.RESET)
			.a(timestamp())
			.a('[');
		if (PRIORITY_COLOR.containsKey(logPriority))
			PRIORITY_COLOR.get(logPriority).accept(ansi);
		ansi.a(logPriority.toString())
			.a(Ansi.Attribute.RESET)
			.a("] ");
		if (s != null)
			ansi.a(s)
				.a('\n');
		if (throwable != null)
			ansi.a(throwable.toString())
				.a('\n')
				.a(ExceptionsKt.stackTraceToString(throwable))
				.a('\n');
		System.out.print(ansi);
		return null;
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
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.VERBOSE, ansi -> ansi.bold().fg(Ansi.Color.MAGENTA));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.DEBUG, ansi -> ansi.bold().fgBright(Ansi.Color.BLUE));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.INFO, ansi -> ansi.bold().fg(Ansi.Color.GREEN));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.WARNING, ansi -> ansi.bold().fg(Ansi.Color.YELLOW));
		PRIORITY_COLOR.put(SimpleLogger.LogPriority.ERROR, ansi -> ansi.bold().fg(Ansi.Color.RED));
	}
}
