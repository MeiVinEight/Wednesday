package org.mve.coroutine;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import org.jetbrains.annotations.NotNull;
import org.mve.uni.Mirroring;

import java.util.concurrent.CompletableFuture;

public class ContinuationWednesday<T> extends CompletableFuture<T> implements Continuation<T>
{
	public final CoroutineContext context;

	public ContinuationWednesday(CoroutineContext context)
	{
		this.context = context;
	}

	public ContinuationWednesday()
	{
		this(EmptyCoroutineContext.INSTANCE);
	}

	@Override
	public void resumeWith(@NotNull Object o)
	{
		this.complete(Mirroring.checkcast(o));
	}

	@NotNull
	@Override
	public CoroutineContext getContext()
	{
		return this.context;
	}

	public void reset()
	{
		this.complete(null);
	}
}
