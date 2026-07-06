package org.mve.web;

import org.mve.uni.Json;

public interface WebService
{
	Object service(Json body);
	default boolean auth()
	{
		return true;
	}
}
