package org.mve.logging;

import kotlin.Lazy;
import net.mamoe.mirai.utils.MiraiLogger;
import org.mve.Wednesday;

public class LoggerLazy implements Lazy<MiraiLogger>
{
	@Override
	public MiraiLogger getValue()
	{
		return Wednesday.LOGGER;
	}

	@Override
	public boolean isInitialized()
	{
		return true;
	}
}
