package org.mve.woden;

import org.mve.mixin.MixinMirroring;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class ResourceEnumeration implements Enumeration<URL>
{
	private final String[] resources;
	private int index = 0;

	public ResourceEnumeration(String[] resources)
	{
		this.resources = resources;
	}

	@Override
	public boolean hasMoreElements()
	{
		return this.index < this.resources.length;
	}

	@Override
	public URL nextElement()
	{
		try
		{
			return new URL(this.resources[this.index++]);
		}
		catch (MalformedURLException e)
		{
			MixinMirroring.sneaking(e);
			throw new RuntimeException(e);
		}
	}
}
