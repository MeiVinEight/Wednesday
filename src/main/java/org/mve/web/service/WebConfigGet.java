package org.mve.web.service;

import org.mve.Configuration;
import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class WebConfigGet implements WebService
{
	@Override
	public Object service(Json body)
	{
		return WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_DATA, new Json()
				.set(WebAPI.KEY_HOST, Configuration.address())
				.set(WebAPI.KEY_PORT, Configuration.port())
			);
	}
}
