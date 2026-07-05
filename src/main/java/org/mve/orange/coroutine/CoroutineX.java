package org.mve.orange.coroutine;

import kotlin.coroutines.AbstractCoroutineContextElement;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineName;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.uni.Mirroring;

public class CoroutineX extends AbstractCoroutineContextElement
{
	public CoroutineX()
	{
		super(CoroutineName.Key);
	}

	@NotNull
	@Override
	public CoroutineContext plus(@NotNull CoroutineContext context)
	{
		return (CoroutineContext) this;
	}

	@Nullable
	@Override
	public <E extends CoroutineContext.Element> E get(CoroutineContext.Key<E> key)
	{
		if ((Object) key == Job.Key)
			return Mirroring.checkcast(new JobX());
		return null;
	}
}
