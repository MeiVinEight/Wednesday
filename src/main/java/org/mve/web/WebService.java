package org.mve.web;

import com.sun.net.httpserver.HttpExchange;
import org.mve.uni.Json;

import java.util.function.Function;

public interface WebService extends Function<Json, Object>
{
	default Object service(Json body)
	{
		return null;
	}

	default Object service(HttpExchange exchange, Json body)
	{
		return this.service(body);
	}

	default boolean auth()
	{
		return true;
	}

	@Override
	default Object apply(Json json)
	{
		return this.service(json);
	}
}
