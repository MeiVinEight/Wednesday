package org.mve.orange.mixin;

import kotlin.jvm.functions.Function2;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.mixin.Shadow;

import java.util.function.Function;

@Mixin(target = "net/mamoe/mirai/event/EventChannel$subscribe$2")
public class EventChannel$subscribe$2Mixin implements Function2<Object, Object, Object>
{
	@Shadow
	private Function<Object, Object> $handler;

	@Overwrite
	@Override
	public Object invoke(Object o, Object o2)
	{
		return this.$handler.apply(o);
	}
}
