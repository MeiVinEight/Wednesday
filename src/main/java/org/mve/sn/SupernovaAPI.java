package org.mve.sn;

import org.mve.sn.core.Supernova;
import org.mve.uni.Json;

public class SupernovaAPI
{
	public static final String KEY_ACTION = "action";
	public static final String KEY_ECHO = "echo";
	public static final String KEY_POST_TYPE = "post_type";
	public static final String KEY_META_EVENT_TYPE = "meta_event_type";
	public static final String KEY_SUBTYPE = "sub_type";
	public static final String KEY_SELF_ID = "self_id";
	public static final String KEY_DATA = "data";
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_APP_VERSION = "app_version";

	public static final String POST_TYPE_META_EVENT = "meta_event";
	public static final String META_EVENT_LIFECYCLE = "lifecycle";
	public static final String LIFECYCLE_CONNECT = "connect";

	public static final String API_GET_VERSION_INFO = "get_version_info";

	public static String getStatus(Supernova ws)
	{
		return "";
	}

	public static Json getVersionInfo(Supernova sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_VERSION_INFO);
		return sn.communicate(json, false);
	}
}
