package org.mve.sn.event;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.sequences.Sequence;
import kotlinx.coroutines.ChildHandle;
import kotlinx.coroutines.ChildJob;
import kotlinx.coroutines.CompletableJob;
import kotlinx.coroutines.CoroutineExceptionHandler;
import kotlinx.coroutines.DisposableHandle;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.SupervisorKt;
import kotlinx.coroutines.selects.SelectClause0;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.uni.Mirroring;

import java.util.concurrent.CancellationException;

public class SubscribeEvent<E extends Event> implements Listener<E>, CoroutineContext.Element
{
	private final CompletableJob job;
	private final CoroutineContext context;
	private final Function2<E, Continuation<? super ListeningStatus>, ListeningStatus> callback;
	private final ConcurrencyKind kind;

	public SubscribeEvent(Job parent, CoroutineContext context, Function2<E, Continuation<? super ListeningStatus>, ListeningStatus> callback, ConcurrencyKind kind)
	{
		this.job = SupervisorKt.SupervisorJob(parent);
		this.context = context;
		this.callback = callback;
		this.kind = kind;
	}

	@NotNull
	@Override
	public ConcurrencyKind getConcurrencyKind()
	{
		return this.kind;
	}

	@Nullable
	@Override
	public ListeningStatus onEvent(@NotNull E e, @NotNull Continuation<? super ListeningStatus> continuation)
	{
		try
		{
			ListeningStatus status = this.callback.invoke(e, continuation);
			if (status == ListeningStatus.STOPPED)
				this.complete();
			return status;
		}
		catch (Throwable t)
		{
			CoroutineExceptionHandler handler = this.context.get(Mirroring.checkcast(CoroutineExceptionHandler.Key));
			if (handler != null)
			{
				handler.handleException(this.context, t);
				return ListeningStatus.LISTENING;
			}

			SupernovaManager.LOGGER.error("未处理的异常", t);
		}
		return ListeningStatus.LISTENING;
	}

	@Override
	public boolean complete()
	{
		return this.job.complete();
	}

	@Override
	public boolean completeExceptionally(@NotNull Throwable throwable)
	{
		return this.job.completeExceptionally(throwable);
	}

	@Override
	public boolean isActive()
	{
		return this.job.isActive();
	}

	@Override
	public boolean isCompleted()
	{
		return this.job.isCompleted();
	}

	@Override
	public boolean isCancelled()
	{
		return this.job.isCancelled();
	}

	@NotNull
	@Override
	public CancellationException getCancellationException()
	{
		return this.job.getCancellationException();
	}

	@Override
	public boolean start()
	{
		return this.job.start();
	}

	@Override
	public void cancel(@Nullable CancellationException e)
	{
		this.job.cancel(e);
	}

	@NotNull
	@Override
	public Sequence<Job> getChildren()
	{
		return this.job.getChildren();
	}

	@SuppressWarnings({"all"})
	@NotNull
	@Override
	public ChildHandle attachChild(@NotNull ChildJob childJob)
	{
		return this.job.attachChild(childJob);
	}

	@Nullable
	@Override
	public Object join(@NotNull Continuation<? super Unit> continuation)
	{
		return this.job.join(continuation);
	}

	@NotNull
	@Override
	public SelectClause0 getOnJoin()
	{
		return this.job.getOnJoin();
	}

	@NotNull
	@Override
	public DisposableHandle invokeOnCompletion(@NotNull Function1<? super Throwable, Unit> function1)
	{
		return this.job.invokeOnCompletion(function1);
	}

	@NotNull
	@Override
	public DisposableHandle invokeOnCompletion(boolean b, boolean b1, @NotNull Function1<? super Throwable, Unit> function1)
	{
		return this.job.invokeOnCompletion(b, b1, function1);
	}

	@SuppressWarnings({"all"})
	@NotNull
	@Override
	public Job plus(@NotNull Job job)
	{
		return this.job.plus(job);
	}

	@Override
	public CoroutineContext minusKey(CoroutineContext.Key<?> key)
	{
		return ((Element) this.job).minusKey(key);
	}

	@Override
	public <R> R fold(R r, Function2<? super R, ? super Element, ? extends R> func2)
	{
		return ((Element) this.job).fold(r, func2);
	}

	@Override
	public <R extends Element> R get(CoroutineContext.Key<R> key)
	{
		return ((Element) this.job).get(key);
	}

	@Override
	public CoroutineContext.Key<?> getKey()
	{
		return ((Element) this.job).getKey();
	}

	@NotNull
	@Override
	public CoroutineContext plus(@NotNull CoroutineContext coroutineContext)
	{
		return ((Element) this.job).plus(coroutineContext);
	}
}
