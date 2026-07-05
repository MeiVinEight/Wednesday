package org.mve.orange.mixin;

import kotlin.jvm.functions.Function2;
import net.mamoe.mirai.event.FilterEventChannel;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.mixin.Shadow;

@Mixin(FilterEventChannel.class)
public class FilterEventChannelMixin implements FilterEventChannelAccessor
{
	@Shadow
	private Function2<Object, Object, Boolean> filter;

	@Overwrite
	@Override
	public Function2<Object, Object, Boolean> filter()
	{
		return this.filter;
	}
}
