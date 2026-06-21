package org.mve.sn.mixin;

import kotlin.jvm.functions.Function2;

public interface FilterEventChannelAccessor
{
	Function2<Object, Object, Boolean> filter();
}
