package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

public class Auth2FAStatus implements WebService
{
	@Override
	public Object service(Json body)
	{
		return WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_ENABLE_2FA, false)
			.set(WebAPI.KEY_HAS_SECRET, false);
	}
}
