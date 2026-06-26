package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.LightApp;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
import org.mve.sn.message.SupernovaMessage;
import org.mve.uni.Json;

@Mixin(LightApp.class)
public class LightAppMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		LightApp $this = (LightApp) (Object) this;
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_JSON)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_DATA, $this.getContent())
			);
	}
}
