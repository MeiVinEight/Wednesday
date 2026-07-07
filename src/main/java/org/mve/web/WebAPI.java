package org.mve.web;

import org.mve.uni.Json;

public class WebAPI
{
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATA = "data";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_SESSION = "session";
	public static final String KEY_TYPE = "type";
	public static final String KEY_STACKTRACE = "stacktrace";
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_ENABLE_2FA = "enable2FA";
	public static final String KEY_HAS_SECRET = "hasSecret";

	public static final int CODE_OK             = 0;
	public static final int CODE_NOT_FOUND      = 1;
	public static final int CODE_ERROR          = 2;
	public static final int CODE_MISSING_PARAM  = 3;
	public static final int CODE_INVALID_TOKEN  = 4;
	public static final int CODE_UNAUTHORIZED   = 5;
	public static final int CODE_INVALID_PARAM  = 6;
	public static final int CODE_RES_NOT_FOUND  = 7;
	public static final int CODE_CONNECT_FAILED = 8;

	public static final String MSG_OK = "OK";
	public static final String MSG_NOT_FOUND = "API Not Found";
	public static final String MSG_ERROR = "Internal Server Error";
	public static final String MSG_MISSING_PARAM = "Missing param: %s";
	public static final String MSG_INVALID_TOKEN = "Invalid Token";
	public static final String MSG_UNAUTHORIZED = "Unauthorized";
	public static final String MSG_INVALID_PARAM = "Invalid parameter: %s";
	public static final String MSG_RES_NOT_FOUND = "Resource Not Found";
	public static final String MSG_CONNECT_FAILED = "Connect failed";
	public static final String[] CODE_MSG;

	public static final String API_LOGIN = "/api/v1/login";
	public static final String API_AUTHCHK = "/api/v1/auth/check";
	public static final String API_TOKEN = "/api/v1/token";
	public static final String API_STOP = "/api/v1/stop";
	public static final String API_CONN = "/api/v1/conn";
	public static final String API_CONFIG = "/api/v1/config";
	public static final String API_AUTH_2FA_STATUS = "/api/v1/auth/2fa/status";

	public static final String COOKIE_JSESSIONID = "JSESSIONID";

	public static String message(String msg, Object... args)
	{
		return String.format(msg, args);
	}

	public static String message(int code, Object... args)
	{
		return message(CODE_MSG[code], args);
	}

	public static Json code(Json body, int code, String msg, Object... args)
	{
		msg = message(msg, args);
		return body.set(KEY_CODE, code)
			.set(KEY_MESSAGE, msg);
	}

	public static Json code(Json body, int code, Object... args)
	{
		return code(body, code, CODE_MSG[code], args);
	}

	static
	{
		CODE_MSG = new String[]{
			MSG_OK,
			MSG_NOT_FOUND,
			MSG_ERROR,
			MSG_MISSING_PARAM,
			MSG_INVALID_TOKEN,
			MSG_UNAUTHORIZED,
			MSG_INVALID_PARAM,
			MSG_RES_NOT_FOUND,
			MSG_CONNECT_FAILED
		};
	}
}
