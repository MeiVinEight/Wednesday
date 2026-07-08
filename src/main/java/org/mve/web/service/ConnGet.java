package org.mve.web.service;

import org.mve.ConnectionManager;
import org.mve.ConnectionWednesday;
import org.mve.uni.Json;
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
		Json wsClients = new Json(Json.TYPE_ARRAY);
		Json data = new Json()
			.set(WebAPI.KEY_NETWORK, new Json()
				.set(WebAPI.KEY_WEBSOCKET_CLIENTS, wsClients)
				.set(WebAPI.KEY_WEBSOCKET_SERVERS, new Json(Json.TYPE_ARRAY))
				.set(WebAPI.KEY_HTTP_CLIENTS, new Json(Json.TYPE_ARRAY))
				.set(WebAPI.KEY_HTTP_SERVERS, new Json(Json.TYPE_ARRAY))
			);
		Json resp = WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_DATA, data);
		for (ConnectionWednesday conn : this.connection.all())
			wsClients.add(conn.data(false));
		return resp;
	}
}
