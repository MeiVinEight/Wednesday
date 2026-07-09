package org.mve.web.service;

import org.mve.web.ConnectionManager;
import org.mve.web.Connection;
import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class ConnPost implements WebService
{
	private final ConnectionManager connection;

	public ConnPost(ConnectionManager connection)
	{
		this.connection = connection;
	}

	@Override
	public Object service(Json body)
	{
		Json arr = body.get(WebAPI.KEY_WEBSOCKET_CLIENTS);
		if (arr != null)
			for (int i = 0; i < arr.length(); i++)
				this.connection.set(Connection.resolve(arr.get(i)));
		arr = body.get(WebAPI.KEY_DELETE);
		if (arr != null)
			for (int i = 0; i < arr.length(); i++)
				this.connection.remove(arr.string(i));
		return null;
	}
}
