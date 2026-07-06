package org.mve.web;

import org.mve.uni.Json;

import java.util.function.Function;

public interface WebService extends Function<Json, Object>
{
	Object service(Json body);

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
