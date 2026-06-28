package org.mve.sn.message.app;

import kotlin.Lazy;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class MiniApp extends QQLightApp
{
	public static final long APP_BILIBILI = 1109937557;
	public final Lazy<Json> detail;
	public final Lazy<String> preview;
	public final Lazy<Long> appid;

	public MiniApp(String content)
	{
		super(content);
		this.detail = new LazyJVM<>(() -> this.meta().get(ILightApp.KEY_DETAIL_1));
		this.preview = new LazyJVM<>(() -> this.meta().string(ILightApp.KEY_PREVIEW));
		this.appid = new LazyJVM<>(() -> Long.parseLong(this.detail().string(ILightApp.KEY_APPID)));
	}

	public Json detail()
	{
		return this.detail.getValue();
	}

	public String preview()
	{
		return this.preview.getValue();
	}

	public long appid()
	{
		return this.appid.getValue();
	}

	public static MiniApp resolve(String content)
	{
		MiniApp app = new MiniApp(content);
		long appid = app.appid();
		if (appid == APP_BILIBILI)
			return new BilibiliApp(content);
		return app;
	}
}
