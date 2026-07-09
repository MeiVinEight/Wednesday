package org.mve.logging;

import kotlin.Lazy;
import net.mamoe.mirai.utils.MiraiLogger;

public class LoggerLazy implements Lazy<MiraiLogger>
{
	@Override
	public MiraiLogger getValue()
	{
		return new WednesdayLogger(null);
	}

	@Override
	public boolean isInitialized()
	{
		return true;
	}
}
