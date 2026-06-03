package org.mve.sn;

import org.mve.sn.core.Supernova;
import org.mve.uni.Json;

public class SupernovaAPI
{
	public static final String KEY_ACTION = "action";
	public static final String KEY_ECHO = "echo";
	public static final String KEY_STATUS = "status";
	public static final String KEY_RETCODE = "retcode";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_POST_TYPE = "post_type";
	public static final String KEY_META_EVENT_TYPE = "meta_event_type";
	public static final String KEY_SUBTYPE = "sub_type";
	public static final String KEY_SELF_ID = "self_id";
	public static final String KEY_DATA = "data";
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_APP_VERSION = "app_version";
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_NICKNAME = "nickname";
	public static final String KEY_REMARK = "remark";
	public static final String KEY_PARAMS = "params";
	public static final String KEY_NO_CACHE = "no_cache";

	public static final String STATUS_OK = "ok";
	public static final String STATUS_FAILED = "failed";

	public static final String POST_TYPE_META_EVENT = "meta_event";
	public static final String META_EVENT_LIFECYCLE = "lifecycle";
	public static final String LIFECYCLE_CONNECT = "connect";

	public static final String API_GET_VERSION_INFO = "get_version_info";
	public static final String API_GET_FRIEND_LIST = "get_friend_list";
	public static final String API_GET_STRANGER_INFO = "get_stranger_info";
	public static final String API_GET_LOGIN_INFO = "get_login_info";

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

	public static Json getFriendList(Supernova sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_FRIEND_LIST);
		return sn.communicate(json, false);
	}

	public static Json getStrangerInfo(Supernova sn, long userId, boolean noCache)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_STRANGER_INFO)
			.set(KEY_PARAMS, new Json()
				.set(KEY_USER_ID, userId)
				.set(KEY_NO_CACHE, noCache)
			);
		return sn.communicate(json, false);
	}

	public static Json getLoginInfo(Supernova sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_LOGIN_INFO);
		return sn.communicate(json, false);
	}
}
