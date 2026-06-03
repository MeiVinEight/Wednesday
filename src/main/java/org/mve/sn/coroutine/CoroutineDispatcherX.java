package org.mve.sn.coroutine;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineDispatcher;
import org.jetbrains.annotations.NotNull;

public class CoroutineDispatcherX extends CoroutineDispatcher
{
	@Override
	public void dispatch(@NotNull CoroutineContext coroutineContext, @NotNull Runnable runnable)
	{
		runnable.run();
	}

	@NotNull
	@Override
	public CoroutineContext plus(@NotNull CoroutineContext context)
	{
		return context;
	}
}
