package org.mve.uni;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CompletionWaiting<T> implements Future<T>
{
	private boolean completion = false;
	private boolean cancelled = false;
	private T value;
	private Throwable exception;

	@Override
	public synchronized boolean cancel(boolean mayInterruptIfRunning)
	{
		if (this.completion)
			return false;
		this.complete(null);
		this.cancelled = true;
		return true;
	}

	@Override
	public boolean isCancelled()
	{
		return this.cancelled;
	}

	@Override
	public boolean isDone()
	{
		return this.completion;
	}

	@Override
	public T get()
	{
		return this.get(true);
	}

	@Override
	public T get(long timeout, @NotNull TimeUnit unit)
	{
		return this.get(timeout, unit, true);
	}

	public T get(boolean interruptible)
	{
		return this.get(-1, TimeUnit.MILLISECONDS, interruptible);
	}

	public synchronized T get(long timeout, TimeUnit unit, boolean interruptible)
	{
		if (!this.completion)
		{
			long ms = (timeout < 0) ? -1 : unit.toMillis(timeout);
			long until = ms > 0 ? (ms + System.currentTimeMillis()) : Long.MAX_VALUE;
			while (!this.completion)
			{
				try
				{
					long now = System.currentTimeMillis();
					if (now < until)
						this.wait(until - now);
					break;
				}
				catch (InterruptedException e)
				{
					if (interruptible)
						Mirroring.thrown(e);
				}
			}
		}
		if (this.exception != null)
			throw new CompletionException(this.exception);
		return this.value;
	}

	public synchronized void complete(T value)
	{
		if (this.completion)
			return;
		this.completion = true;
		this.value = value;
		this.notifyAll();
	}

	public synchronized void exception(Throwable t)
	{
		Throwable cause = t;
		while (cause.getCause() != null)
			cause = cause.getCause();
		cause.initCause(this.exception);
		this.exception = t;
	}
}
