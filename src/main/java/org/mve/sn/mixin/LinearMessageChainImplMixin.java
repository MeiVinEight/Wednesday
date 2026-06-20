package org.mve.sn.mixin;

import net.mamoe.mirai.message.data.LinearMessageChainImpl;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.sn.message.MessageJson;
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
		chain.stream().filter(e -> e instanceof MessageJson).forEach(s -> json.add(((MessageJson) s).json()));
		return json;
	}
}
