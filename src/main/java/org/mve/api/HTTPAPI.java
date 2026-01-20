package org.mve.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HTTPAPI
{
	public static File download(String url, Map<String, String> headers, File file) throws IOException
	{
		if (url == null)
			throw new NullPointerException("URL");
		URL url1 = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
		if (headers != null)
		{
			headers.forEach((k, v) ->
			{
				if (v != null)
					conn.setRequestProperty(k, v);
			});
		}
		int code = conn.getResponseCode();
		if (code != 200)
			throw new IOException("HTTP CODE " + code);
		if (file == null)
			file = new File(new File(url1.getPath()).getName());
		if (!file.isDirectory())
		{
			boolean ignored = file.getParentFile().mkdirs();
		}
		else
			file = new File(file, new File(url1.getPath()).getName());
		try (InputStream in = conn.getInputStream(); FileOutputStream fout = new FileOutputStream(file))
		{
			in.transferTo(fout);
		}
		return file;
	}

	public static byte[] body(HttpURLConnection conn) throws IOException
	{
		try (InputStream in = conn.getInputStream())
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			in.transferTo(out);
			return out.toByteArray();
		}
	}
}
