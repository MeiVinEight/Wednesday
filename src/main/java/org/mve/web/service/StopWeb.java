package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;
import org.mve.web.WednesdayWeb;

public class StopWeb implements WebService
{
	@Override
	public Object service(Json body)
	{
		WednesdayWeb.web.close();
		return WebAPI.code(new Json(), WebAPI.CODE_OK);
	}
}
