package org.mve.sn.message.app;

import kotlin.Lazy;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public abstract class QQLightApp extends BaseLightApp
{
	public final Lazy<String> prompt;
	public final Lazy<Json> meta;

	public QQLightApp(String content)
	{
		super(content);
		this.prompt = new LazyJVM<>(() -> this.data().string(ILightApp.KEY_PROMPT));
		this.meta = new LazyJVM<>(() -> this.data().get(ILightApp.KEY_META));
	}

	public String prompt()
	{
		return this.prompt.getValue();
	}

	public Json meta()
	{
		return this.meta.getValue();
	}
}
