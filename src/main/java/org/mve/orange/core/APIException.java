package org.mve.orange.core;

public class APIException extends RuntimeException
{
	public final APIResponse response;

	public APIException(APIResponse response)
	{
		super(response.message);
		this.response = response;
	}
}
