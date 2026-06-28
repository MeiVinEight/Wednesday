package org.mve.sn.core;

import org.mve.sn.SupernovaAPI;
import org.mve.uni.Json;

public class APIResponse
{
	public static final int STATUS_UNKNOWN = 0;
	public static final int STATUS_OK = 1;
	public static final int STATUS_ASYNC = 2;
	public static final int STATUS_FAILED = 3;

	public final Json origin;
	public final int code;
	public final Number echo;
	public final int status;
	public final String message;
	public final Json data;

	public APIResponse(Json origin)
	{
		this.origin = origin;
		this.code = origin.contains(SupernovaAPI.KEY_RETCODE) ? origin.number(SupernovaAPI.KEY_RETCODE).intValue() : 0;
		Number echo = null;
		try
		{
			echo = origin.number(SupernovaAPI.KEY_ECHO);
		}
		catch (Throwable ignored)
		{
		}
		this.echo = echo;
		int status = STATUS_UNKNOWN;
		try
		{
			String statTxt = origin.string(SupernovaAPI.KEY_STATUS);
			if (SupernovaAPI.STATUS_OK.equals(statTxt))
				status = STATUS_OK;
			if (SupernovaAPI.STATUS_FAILED.equals(statTxt))
				status = STATUS_FAILED;
			if (SupernovaAPI.STATUS_ASYNC.equals(statTxt))
				status = STATUS_ASYNC;
		}
		catch (Throwable ignored)
		{
		}
		this.status = status;
		String msg = null;
		try
		{
			msg = origin.string(SupernovaAPI.KEY_MESSAGE);
		}
		catch (Throwable ignored)
		{
		}
		this.message = msg;
		this.data = origin.get(SupernovaAPI.KEY_DATA);
	}

	public void checkValidation()
	{
		if (this.status != STATUS_OK)
			throw new APIException(this);
	}
}
