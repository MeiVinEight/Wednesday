package org.mve.logging;

import kotlin.jvm.functions.Function0;
import kotlin.properties.ReadWriteProperty;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.uni.Mirroring;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class LoggerManager implements ILoggerFactory, MiraiLogger.Factory
{
	public static final LoggerManager FACTORY;

	public static Coffee create()
	{
		return new Coffee(null);
	}

	public static Coffee create(String name)
	{
		return new Coffee(name);
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
