package org.mve.sn.coroutine;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;

public class ContinuationS<T> implements Continuation<T>
{
	private final CoroutineContext context;

	public ContinuationS(CoroutineContext context)
	{
		this.context = context;
	}

	@Override
	public void resumeWith(@NotNull Object o)
	{
	}

	@NotNull
	@Override
	public CoroutineContext getContext()
	{
		return this.context;
	}
}
