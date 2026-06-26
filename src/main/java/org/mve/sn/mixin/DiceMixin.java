package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.Dice;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
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
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_DICE)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_RESULT, String.valueOf(dice.getValue()))
			);
	}
}
