package org.mve.uni;

import java.security.MessageDigest;

public class MD5
{
	public static MessageDigest get()
	{
		try
		{
			return MessageDigest.getInstance("MD5");
		}
		catch (Throwable t)
		{
			Mirroring.thrown(t);
			throw new NullPointerException();
		}
	}
}
