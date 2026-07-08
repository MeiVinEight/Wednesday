package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;
import org.mve.web.WednesdayWeb;

public class Version implements WebService
{
	@Override
	public Object service(Json body)
	{
		return WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_DATA, new Json()
				.set(WebAPI.KEY_VERSION, WednesdayWeb.version())
			);
	}
}
