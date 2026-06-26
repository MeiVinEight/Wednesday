package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.RockPaperScissors;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
import org.mve.uni.Json;

@Mixin(RockPaperScissors.class)
public class RockPaperScissorsMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		RockPaperScissors rps = (RockPaperScissors) (Object) this;
		int result = 0;
		if (rps == RockPaperScissors.ROCK)
			result = 1;
		if (rps == RockPaperScissors.SCISSORS)
			result = 2;
		if (rps == RockPaperScissors.PAPER)
			result = 3;
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_RPS)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_RESULT, String.valueOf(result))
			);
	}
}
