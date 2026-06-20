package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.PlainText;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
import org.mve.uni.Json;

@Mixin(PlainText.class)
public class PlainTextMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		PlainText text = (PlainText) (Object) this;
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_TEXT)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_TEXT, text.toString())
			);
	}
}
