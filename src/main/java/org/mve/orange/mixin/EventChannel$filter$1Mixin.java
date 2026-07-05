package org.mve.orange.mixin;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.mixin.Shadow;

@Mixin(target = "net/mamoe/mirai/event/EventChannel$filter$1")
public class EventChannel$filter$1Mixin implements Function2<Object, Object, Object>
{
	@Shadow
	private Function1<Object, Object> $filter;

	@Overwrite
	@Override
	public Object invoke(Object o, Object o2)
	{
		return this.$filter.invoke(o);
	}
}
