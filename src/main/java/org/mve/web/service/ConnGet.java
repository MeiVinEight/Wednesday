package org.mve.web.service;

import org.mve.ConnectionManager;
import org.mve.ConnectionWednesday;
import org.mve.uni.Json;
import org.mve.web.APIException;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class ConnGet implements WebService
{
	private final ConnectionManager connection;

	public ConnGet(ConnectionManager connection)
	{
		this.connection = connection;
	}

	@Override
	public Object service(Json body)
	{
		Json data = new Json(Json.TYPE_ARRAY);
		Json resp = WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_DATA, data);
		Json names = body.get(WebAPI.KEY_NAME);
		if (names == null)
		{
			for (ConnectionWednesday conn : this.connection.all())
				data.add(conn.data());
			return resp;
		}
		if (names.type == Json.TYPE_STRING)
			names = new Json(Json.TYPE_ARRAY).add(names.string());
		if (names.type == Json.TYPE_ARRAY)
		{
			for (int i = 0; i < names.length(); i++)
			{
				String name = names.string(i);
				ConnectionWednesday conn = this.connection.get(name);
				if (conn != null)
					data.add(conn.data());
			}
			return resp;
		}
		throw new APIException(WebAPI.CODE_INVALID_PARAM, (Object) WebAPI.KEY_NAME);
	}
}
