package org.mve.sn.coroutine;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.sequences.Sequence;
import kotlinx.coroutines.ChildHandle;
import kotlinx.coroutines.ChildJob;
import kotlinx.coroutines.DisposableHandle;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.selects.SelectClause0;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;

public class JobX implements Job, CoroutineContext.Element
{
	@NotNull
	@Override
	public Sequence<Job> getChildren()
	{
		return null;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public boolean isCompleted()
	{
		return false;
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@NotNull
	@Override
	public CancellationException getCancellationException()
	{
		return new CancellationException();
	}

	@Override
	public boolean start()
	{
		return false;
	}

	@Override
	public void cancel(@Nullable CancellationException e)
	{

	}

	@NotNull
	@Override
	public ChildHandle attachChild(@NotNull ChildJob childJob)
	{
		return null;
	}

	@Nullable
	@Override
	public Object join(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public SelectClause0 getOnJoin()
	{
		return null;
	}

	@NotNull
	@Override
	public DisposableHandle invokeOnCompletion(@NotNull Function1<? super Throwable, Unit> function1)
	{
		return null;
	}

	@NotNull
	@Override
	public DisposableHandle invokeOnCompletion(boolean b, boolean b1, @NotNull Function1<? super Throwable, Unit> function1)
	{
		return null;
	}

	@NotNull
	@Override
	public Job plus(@NotNull Job job)
	{
		return null;
	}

	@NotNull
	@Override
	public CoroutineContext.Key<?> getKey()
	{
		return null;
	}

	@Nullable
	@Override
	public <E extends Element> E get(@NotNull CoroutineContext.Key<E> key)
	{
		return null;
	}

	@Override
	public <R> R fold(R r, @NotNull Function2<? super R, ? super Element, ? extends R> function2)
	{
		return null;
	}

	@NotNull
	@Override
	public CoroutineContext minusKey(@NotNull CoroutineContext.Key<?> key)
	{
		return null;
	}

	@NotNull
	@Override
	public CoroutineContext plus(@NotNull CoroutineContext coroutineContext)
	{
		return null;
	}
}
