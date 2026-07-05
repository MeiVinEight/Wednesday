package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.PlainText;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
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
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_TEXT)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_TEXT, text.toString())
			);
	}
}
