package org.mve.orange.mixin;

import net.mamoe.mirai.event.ListeningStatus;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(target = "net/mamoe/mirai/event/EventChannel$subscribeOnce$2")
public class EventChannel$subscribeOnce$2Mixin
{
	@Shadow
	private Consumer<Object> $handler;

	@Overwrite
	public Object invoke(Object a, Object b)
	{
		this.$handler.accept(a);
		return ListeningStatus.STOPPED;
	}
}
