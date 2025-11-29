package org.mve;

import kotlinx.coroutines.DefaultExecutor;
import kotlinx.coroutines.scheduling.CoroutineScheduler;
import kotlinx.coroutines.scheduling.CoroutineSchedulerKt;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.FileLogger;
import org.mve.logging.LoggerManager;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main
{
	private static final Object WAITING = new Object();

	public static void main(String[] args)
	{
		System.setProperty("kotlinx.coroutines.scheduler.keep.alive.sec", "1");
		LoggerManager.register(FileLogger.INSTANCE);
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

		try
		{
			Wednesday wednesday = new Wednesday();
			wednesday.join();
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
		}
		Thread current = Thread.currentThread();
		Thread[] threads = new Thread[64];
		int count;
		for (;;)
		{
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			count = group.enumerate(threads);
			int nonDaemon = Stream.of(threads)
				.filter(Objects::nonNull)
				.filter(Predicate.not(Predicate.isEqual(current)))
				.filter(Predicate.not(Thread::isDaemon))
				.toArray()
				.length;
			if (nonDaemon == 0)
				break;
			synchronized (WAITING)
			{
				try
				{
					WAITING.wait(1000);
				}
				catch (InterruptedException e)
				{
					Wednesday.LOGGER.warn("", e);
				}
			}
		}
		DefaultExecutor.INSTANCE.shutdown();
		for (int i = 0; i < count; i++)
		{
			Thread key =  threads[i];
			Wednesday.LOGGER.trace("Interrupt {}", key.getName());
			if (key.isDaemon())
			{
				if (CoroutineSchedulerKt.isSchedulerWorker(key))
					((CoroutineScheduler.Worker) key).state = CoroutineScheduler.WorkerState.TERMINATED;
				key.interrupt();
			}
		}
		FileLogger.INSTANCE.close();
	}
}
