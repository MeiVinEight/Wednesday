package org.mve;

import net.mamoe.mirai.utils.SimpleLogger;
import org.fusesource.jansi.Ansi;

import java.lang.invoke.MethodHandle;

public class Main
{
	public static void main(String[] args)
	{
		Wednesday.LOGGER.info("Wednesday 启动");
		Wednesday.LOGGER.info("进行修补:");

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
				Wednesday.LOGGER.info("日志等级:{}", Ansi.ansi().bold().fg(Ansi.Color.GREEN).a("修补完成").reset());
			}
			catch (Throwable e)
			{
				Wednesday.LOGGER.warn("日志等级:{}", Ansi.ansi().bold().fg(Ansi.Color.RED).a("修补失败").reset(), e);
			}
		}

		Wednesday wednesday = new Wednesday();
		Runtime.getRuntime().addShutdownHook(new Thread(wednesday::close));
	}
}
