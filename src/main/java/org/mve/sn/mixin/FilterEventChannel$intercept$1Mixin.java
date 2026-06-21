package org.mve.sn.mixin;

import kotlin.jvm.functions.Function2;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.FilterEventChannel;
import net.mamoe.mirai.event.ListeningStatus;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.mixin.Shadow;

@Mixin(target = "net/mamoe/mirai/event/FilterEventChannel$intercept$1")
public class FilterEventChannel$intercept$1Mixin
{
	@Shadow
	private FilterEventChannel<? extends Event> this$0;
	@Shadow
	private Function2<Object, Object, Object> $block;

	@Overwrite
	public Object invoke(Object event, Object target)
	{
		if (!this.this$0.getBaseEventClass().isInstance(event))
			return ListeningStatus.LISTENING;
		Boolean val = ((FilterEventChannelAccessor) this.this$0).filter().invoke(event, target);
		if (val == null)
			return ListeningStatus.LISTENING;
		if (!val)
			return ListeningStatus.LISTENING;
		return this.$block.invoke(event, target);
	}
}
