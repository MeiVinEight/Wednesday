package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.QuoteReply;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
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
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_REPLY)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_ID, String.valueOf(reply.getSource().getIds()[0]))
			);
	}
}
