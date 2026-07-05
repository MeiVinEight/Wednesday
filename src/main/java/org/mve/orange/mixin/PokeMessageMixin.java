package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.PokeMessage;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
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
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_POKE)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_ID, String.valueOf(poke.getId()))
				.set(OrangeMessage.KEY_TYPE, String.valueOf(poke.getPokeType()))
			);
	}
}
