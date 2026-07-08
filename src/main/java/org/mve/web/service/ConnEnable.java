package org.mve.web.service;

import org.mve.ConnectionManager;
import org.mve.ConnectionWednesday;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;
import org.mve.uni.Mirroring;
import org.mve.web.APIException;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class ConnEnable implements WebService
{
	private final ConnectionManager connection;

	public ConnEnable(ConnectionManager connection)
	{
		this.connection = connection;
	}

	@Override
	public Object service(Json body)
	{
		String name = body.string(WebAPI.KEY_NAME);
		Boolean enable = body.bool(WebAPI.KEY_ENABLE);
		if (name == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_NAME);
		if (enable == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_ENABLE);
		ConnectionWednesday conn = this.connection.get(name);
		if (conn == null)
			throw new APIException(WebAPI.CODE_INVALID_PARAM, (Object) WebAPI.KEY_NAME);
		if (enable)
		{
			Orange result = conn.connect(false);
			if (result == null)
			{
				Throwable err = conn.error();
				if (err != null)
					Mirroring.thrown(err);
				throw new APIException(WebAPI.CODE_ERROR);
			}
		}
		else
		{
			conn.close();
		}
		return null;
	}
}
