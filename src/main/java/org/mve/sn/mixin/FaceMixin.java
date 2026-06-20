package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.Face;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
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
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_FACE)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_ID, String.valueOf($this.getId()))
			);
	}
}
