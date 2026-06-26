package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.PokeMessage;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
import org.mve.uni.Json;

@Mixin(PokeMessage.class)
public class PokeMessageMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		PokeMessage poke = (PokeMessage) (Object) this;
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_POKE)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_ID, String.valueOf(poke.getId()))
				.set(SupernovaMessage.KEY_TYPE, String.valueOf(poke.getPokeType()))
			);
	}
}
