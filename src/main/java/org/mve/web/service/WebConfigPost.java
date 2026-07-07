package org.mve.web.service;

import org.mve.Configuration;
import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class WebConfigPost implements WebService
{
	@Override
	public Object service(Json body)
	{
		String host = body.string(WebAPI.KEY_HOST);
		if (host != null)
			Configuration.address(host);
		Number port = body.number(WebAPI.KEY_PORT);
		if (port != null)
			Configuration.port(port.intValue());
		return null;
	}
}
