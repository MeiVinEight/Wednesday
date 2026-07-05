package org.mve.orange.message.app;

import kotlin.Lazy;
import org.mve.uni.LazyJVM;

public class BilibiliApp extends MiniApp
{
	private final Lazy<String> url;

	public BilibiliApp(String content)
	{
		super(content);
		this.url = new LazyJVM<>(() -> this.data().string(ILightApp.KEY_QQDOCURL));
	}

	public String url()
	{
		return this.url.getValue();
	}
}
