package org.mve.web;

public class APIException extends RuntimeException
{
	public final int code;
	public final String message;

	public APIException(int code, String msg, Object... args)
	{
		super(WebAPI.message(msg, args));
		this.code = code;
		this.message = this.getMessage();
	}

	public APIException(int code, Object... args)
	{
		this(code, WebAPI.CODE_MSG[code], args);
	}
}
