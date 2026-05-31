package org.mve.mixin;

import cn.evolvefield.onebot.client.handler.ActionHandler;
import com.google.gson.JsonObject;
import org.mve.Wednesday;

@Mixin(ActionHandler.class)
public class ActionHandlerMixin
{
	@Inject(value = "onReceiveActionResp(Lcom/google/gson/JsonObject;)V", at = Inject.AT_HEAD)
	public void onReceiveActionResp(JsonObject respJson, Callback call)
	{
		Wednesday.LOGGER.warn("这是Mixin方法 {}", respJson, new Throwable());
	}
}
