package org.mve.mixin;

import cn.evolvefield.onebot.client.connection.IAdapter;
import com.google.gson.JsonObject;
import org.mve.uni.Mirroring;

@Mixin(IAdapter.DefaultImpls.class)
public class IAdaptorMixin
{
	@Inject(
		value = "onReceiveMessage(Lcn/evolvefield/onebot/client/connection/IAdapter;Ljava/lang/String;)V",
		at = Inject.AT_INVOKE,
		method = "Lcn/evolvefield/onebot/client/handler/ActionHandler;onReceiveActionResp(Lcom/google/gson/JsonObject;)V"
	)
	public static void onReceiveMessage(IAdapter adapter, String message, JsonObject data, Callback callback)
	{
		if ("ok".equals(data.get("status").getAsString()))
			return;
		int code = data.get("retcode").getAsInt();
		String msg = data.get("message").getAsString();
		Mirroring.thrown(new RuntimeException(code + ": " + msg));
	}
}
