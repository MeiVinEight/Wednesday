package org.mve.web.service;

import com.sun.net.httpserver.HttpExchange;
import org.mve.uni.HTTP;
import org.mve.uni.Json;
import org.mve.web.APIException;
import org.mve.web.Persistence;
import org.mve.web.WebAPI;
import org.mve.web.WebService;

import java.util.List;

public class WebPersistence implements WebService
{
	@Override
	public boolean auth()
	{
		return false;
	}

	@Override
	public Object service(HttpExchange exchange, Json body)
	{
		Json resp = WebAPI.code(new Json(), WebAPI.CODE_OK);
		String action = body.string(WebAPI.KEY_ACTION);
		String name = body.string(WebAPI.KEY_NAME);
		if (action == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_ACTION);
		if (name == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_NAME);
		if (action.equals(HTTP.METHOD_GET))
		{
			List<Persistence> pers = Persistence.MAPPER.select(Persistence.class, "NAME", name);
			if (pers.isEmpty())
				throw new APIException(WebAPI.CODE_INVALID_PARAM, (Object) WebAPI.KEY_NAME);
			Persistence per = pers.get(0);
			if (per.auth() && !WebAPI.authenticate(exchange))
				throw new APIException(WebAPI.CODE_UNAUTHORIZED);
			String text = per.TEXT;
			Json data = null;
			if (text != null)
				data = Json.resolve(text);
			resp.set(WebAPI.KEY_DATA, data);
		}
		else if (action.equals(HTTP.METHOD_POST))
		{
			Json data = body.get(WebAPI.KEY_DATA);
			Boolean auth = body.bool(WebAPI.KEY_AUTH);
			auth = (auth != null) ? auth : false;
			boolean insert = Persistence.MAPPER.select(Persistence.class, "NAME", name).isEmpty();
			if (data == null)
			{
				if (insert)
					throw new APIException(WebAPI.CODE_INVALID_PARAM, (Object) WebAPI.KEY_NAME);
				Persistence.MAPPER.delete(Persistence.class, "NAME", name);
			}
			else if (insert)
				Persistence.MAPPER.insert(new Persistence(name, data.stringify(), auth));
			else
				Persistence.MAPPER.update(new Persistence(name, data.stringify(), auth));
		}
		return resp;
	}
}
