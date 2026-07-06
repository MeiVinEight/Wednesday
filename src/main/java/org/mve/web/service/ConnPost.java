package org.mve.web.service;

import org.mve.ConnectionManager;
import org.mve.ConnectionWednesday;
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
		String name = body.string(WebAPI.KEY_NAME);
		String url = body.string(WebAPI.KEY_URL);
		String token = body.string(WebAPI.KEY_TOKEN);
		this.connection.set(new ConnectionWednesday(name, url, token));
		return null;
	}
}
