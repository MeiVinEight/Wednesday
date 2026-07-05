package org.mve.orange.mixin;

import net.mamoe.mirai.message.data.LinearMessageChainImpl;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.MessageJson;
import org.mve.uni.Json;

@Mixin(LinearMessageChainImpl.class)
public class LinearMessageChainImplMixin implements MessageJson
{
	@Overwrite
	@Override
	public Json json()
	{
		Json json = new Json(Json.TYPE_ARRAY);
		LinearMessageChainImpl chain = (LinearMessageChainImpl) (Object) this;
		chain.forEach(s ->
		{
			if (s instanceof MessageJson)
				json.add(((MessageJson) s).json());
		});
		return json;
	}
}
