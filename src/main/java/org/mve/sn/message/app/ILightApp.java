package org.mve.sn.message.app;

import org.mve.uni.Json;

public interface ILightApp
{
	public static final String APP_MINIAPP01 = "com.tencent.miniapp_01";
	public static final String APP_MUSIC_LUA = "com.tencent.music.lua";
	public static final String KEY_APP = "app";
	public static final String KEY_VER = "ver";
	public static final String KEY_PROMPT = "prompt";
	public static final String KEY_META = "meta";
	public static final String KEY_DETAIL_1 = "detail_1";
	public static final String KEY_PREVIEW = "preview";
	public static final String KEY_APPID = "appid";
	public static final String KEY_QQDOCURL = "qqdocurl";
	public static final String KEY_MUSIC = "music";
	public static final String KEY_TAG = "tag";
	public static final String KEY_JUMPURL = "jumpUrl";
	public static final String KEY_MUSICURL = "musicUrl";

	Json data();
	String app();
	String version();
}
