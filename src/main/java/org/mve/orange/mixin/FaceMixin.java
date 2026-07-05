package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.Face;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.OrangeMessage;
import org.mve.uni.Json;

@Mixin(Face.class)
public class FaceMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		Face $this = (Face) (Object) this;
		return new Json()
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_FACE)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_ID, String.valueOf($this.getId()))
			);
	}
}
