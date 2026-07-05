package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.At;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
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
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_AT)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_QQ, String.valueOf($this.getTarget()))
			);
	}
}
