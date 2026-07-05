package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.Dice;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
import org.mve.uni.Json;

@Mixin(Dice.class)
public class DiceMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		Dice dice = (Dice) (Object) this;
		return new Json()
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_DICE)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_RESULT, String.valueOf(dice.getValue()))
			);
	}
}
