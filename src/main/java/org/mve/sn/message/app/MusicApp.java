package org.mve.sn.message.app;

import kotlin.Lazy;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class MusicApp extends QQLightApp
{
	private final Lazy<String> preview;
	private final Lazy<String> music;
	private final Lazy<String> jump;
	private final Lazy<String> tag;

	public MusicApp(String content)
	{
		super(content);
		this.preview = new LazyJVM<>(() -> this.meta().string(ILightApp.KEY_PREVIEW));
		this.music = new LazyJVM<>(() -> this.meta().string(ILightApp.KEY_MUSICURL));
		this.jump = new LazyJVM<>(() -> this.meta().string(ILightApp.KEY_JUMPURL));
		this.tag = new LazyJVM<>(() -> this.meta().string(KEY_TAG));
	}

	@Override
	public Json meta()
	{
		Json meta = super.meta();
		return meta.get(ILightApp.KEY_MUSIC);
	}

	public String preview()
	{
		return this.preview.getValue();
	}

	public String music()
	{
		return this.music.getValue();
	}

	public String jump()
	{
		return this.jump.getValue();
	}

	public String tag()
	{
		return this.tag.getValue();
	}
}
