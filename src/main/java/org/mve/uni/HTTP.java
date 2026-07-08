package org.mve.uni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class HTTP
{
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_SET_COOKIE = "Set-Cookie";

	public static Throwable download(String url, String path)
	{
		try
		{
			File file = new File(path);
			File dir = file.getParentFile().getAbsoluteFile();
			boolean ignored = dir.mkdirs();
			URL urlc = new URL(url);
			URLConnection conn = urlc.openConnection();
			try (InputStream in = conn.getInputStream(); OutputStream out = new FileOutputStream(file))
			{
				in.transferTo(out);
			}
		}
		catch (Throwable t)
		{
			return t;
		}
		return null;
	}
}
