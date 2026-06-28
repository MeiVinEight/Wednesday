package org.mve.sn.message.app;

import kotlin.Lazy;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public abstract class BaseLightApp implements ILightApp
{
	public final String content;
	public final Lazy<Json> data;
	public final Lazy<String> app;
	public final Lazy<String> version;

	public BaseLightApp(String content)
	{
		this.content = content;
		this.data = new LazyJVM<>(() -> Json.resolve(this.content));
		this.app = new LazyJVM<>(() -> this.data().string(ILightApp.KEY_APP));
		this.version = new LazyJVM<>(() -> this.data().string(ILightApp.KEY_VER));
	}

	@Override
	public Json data()
	{
		return this.data.getValue();
	}

	@Override
	public String app()
	{
		return this.app.getValue();
	}

	@Override
	public String version()
	{
		return this.version.getValue();
	}
}
