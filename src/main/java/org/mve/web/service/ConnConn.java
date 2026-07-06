package org.mve.web.service;

import org.mve.ConnectionManager;
import org.mve.ConnectionWednesday;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;
import org.mve.web.APIException;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class ConnConn implements WebService
{
	private final ConnectionManager connection;

	public ConnConn(ConnectionManager connection)
	{
		this.connection = connection;
	}

	@Override
	public Object service(Json body)
	{
		String name = body.string(WebAPI.KEY_NAME);
		if (name == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_NAME);
		ConnectionWednesday conn = this.connection.get(name);
		if (conn == null)
			throw new APIException(WebAPI.CODE_RES_NOT_FOUND);
		Orange connect = conn.connect(false);
		if (connect == null)
			throw new APIException(WebAPI.CODE_CONNECT_FAILED);
		return null;
	}
}
