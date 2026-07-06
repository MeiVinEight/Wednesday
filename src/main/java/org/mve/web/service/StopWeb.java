package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;
import org.mve.web.WednesdayWeb;

public class StopWeb implements WebService
{
	private final WednesdayWeb web;

	public StopWeb(WednesdayWeb web)
	{
		this.web = web;
	}

	@Override
	public Object service(Json body)
	{
		this.web.close();
		return WebAPI.code(new Json(), WebAPI.CODE_OK);
	}
}
