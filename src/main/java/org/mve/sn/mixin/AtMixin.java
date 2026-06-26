package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.At;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
import org.mve.uni.Json;

@Mixin(At.class)
public class AtMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		At $this = (At) (Object) this;
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_AT)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_QQ, String.valueOf($this.getTarget()))
			);
	}
}
