package org.mve.service;

import org.mve.logging.SystemLogging;

public class SystemLoggingFactory extends System.LoggerFinder
{
	@Override
	public System.Logger getLogger(String name, Module module)
	{
		return new SystemLogging(name);
	}
}
