package org.mve.orange.mixin;

import kotlin.Lazy;
import net.mamoe.mirai.message.data.LightApp;
import org.mve.mixin.Callback;
import org.mve.mixin.Inject;
import org.mve.mixin.Mixin;
import org.mve.mixin.Overwrite;
import org.mve.orange.message.app.ILightApp;
import org.mve.orange.message.MessageJson;
import org.mve.orange.message.app.MessageLightApp;
import org.mve.orange.message.OrangeMessage;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

@Mixin(LightApp.class)
public class LightAppMixin implements MessageJson, MessageLightApp
{
	private ILightApp app;
	private Lazy<Json> data;

	@Inject(value = "<init>(Ljava/lang/String;)V", at = Inject.AT_RETURN)
	private void init(String s, Callback callback)
	{
		this.data = new LazyJVM<>(() -> Json.resolve(((LightApp) (Object) this).getContent()));
	}

	@Overwrite
	@Override
	public Json json()
	{
		LightApp $this = (LightApp) (Object) this;
		return new Json()
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_JSON)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_DATA, $this.getContent())
			);
	}

	@Overwrite
	@Override
	public Json data()
	{
		return this.data.getValue();
	}

	@Overwrite
	@Override
	public ILightApp app()
	{
		if (this.app == null)
		{
			LightApp $this = (LightApp) (Object) this;
			Json data = this.data.getValue();
			String type = data.string(ILightApp.KEY_APP);
			this.app = OrangeMessage.resolveApp(type, $this.getContent());
		}
		return this.app;
	}
}
