package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.QuoteReply;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
import org.mve.uni.Json;

@Mixin(QuoteReply.class)
public class ReplyMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		QuoteReply reply = (QuoteReply) (Object) this;
		return new Json()
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_REPLY)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_ID, String.valueOf(reply.getSource().getIds()[0]))
			);
	}
}
