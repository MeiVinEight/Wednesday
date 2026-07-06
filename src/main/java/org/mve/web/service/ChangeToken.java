package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;
import org.mve.web.WednesdayWeb;

public class ChangeToken implements WebService
{
	@Override
	public Object service(Json exchange)
	{
		WednesdayWeb.token(exchange.string(WebAPI.KEY_TOKEN));
		return WebAPI.code(new Json(), WebAPI.CODE_OK);
	}
}
