package org.mve.logging;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function4;
import kotlin.properties.ReadWriteProperty;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge;
import net.mamoe.mirai.utils.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.uni.Mirroring;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoggerManager implements ILoggerFactory, MiraiLogger.Factory
{
	public static final LoggerManager FACTORY;
	public static final List<Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit>> FUNCTION = new ArrayList<>();

	public static void register(Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function)
	{
		LoggerManager.FUNCTION.add(function);
	}

	public static WednesdayLogger create()
	{
		WednesdayLogger logger = new WednesdayLogger(null);
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : LoggerManager.FUNCTION)
			logger.consumation(function);
		return logger;
	}

	public static WednesdayLogger create(String name)
	{
		WednesdayLogger logger = new WednesdayLogger(name);
		for (Function4<WednesdayLogger, SimpleLogger.LogPriority, String, Throwable, Unit> function : LoggerManager.FUNCTION)
			logger.consumation(function);
		return logger;
	}

	@Override
	public Logger getLogger(String name)
	{
		return create(name);
	}

	@NotNull
	@Override
	public MiraiLogger create(@NotNull Class<?> aClass, @Nullable String s)
	{
		return create(s);
	}

	static
	{
		FACTORY = new LoggerManager();
		ReadWriteProperty<Object, MiraiLogger.Factory> property = Mirroring.get(
			MiraiLoggerFactoryImplementationBridge.class,
			"_instance$delegate",
			ReadWriteProperty.class
		);
		Function0<MiraiLogger.Factory> initializer = () -> LoggerManager.FACTORY;
		Mirroring.set(property.getClass(), "initializer", Function0.class, property, initializer);
	}
}
