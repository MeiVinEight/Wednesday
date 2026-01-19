package org.mve.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BilibiliAPI
{
	public static final String HEADER_LOCATION = "Location";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_REFERER = "Referer";
	public static final String API_VIEW = "https://api.bilibili.com/x/web-interface/view";
	public static final String API_VIEW_BVID = "bvid";
	public static final String API_PLAYURL = "https://api.bilibili.com/x/player/wbi/playurl";
	public static final String API_PLAYURL_BVID = "bvid";
	public static final String API_PLAYURL_CID = "cid";
	public static final String API_PLAYURL_GAIA_SOURCE = "gaia_source";

	public static String view(String urlText) throws Throwable
	{
		if (urlText == null)
			throw new NullPointerException("URL");
		URL url = new URL(urlText);

		if ("b23.tv".equals(url.getHost()))
		{
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			try
			{
				conn.setInstanceFollowRedirects(false);
				conn.connect();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
					try (InputStream in = conn.getInputStream())
					{
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						in.transferTo(bos);
						return bos.toString(StandardCharsets.UTF_8);
					}

				if ((conn.getResponseCode() / 100) != 3)
					throw new IOException("Wrong response code: " + conn.getResponseCode());

				urlText = conn.getHeaderField(HEADER_LOCATION);
			}
			finally
			{
				conn.disconnect();
			}
			if (urlText == null)
				throw new NullPointerException("NO Redirect Location");
		}

		url = new URL(urlText);
		String bvid = url.getPath().substring("/video/".length());

		urlText = API_VIEW + '?' + API_VIEW_BVID + '=' + bvid;
		url = new URL(urlText);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try (InputStream input = conn.getInputStream())
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			input.transferTo(bos);
			return bos.toString(StandardCharsets.UTF_8);
		}
		finally
		{
			conn.disconnect();
		}
	}

	public static String playurl(String bvid, long cid) throws Throwable
	{
		if (bvid == null)
			throw new NullPointerException("bvid");
		if (cid < 0)
			throw new IllegalArgumentException("cid < 0");
		String gaia_source = "pre-load";
		String urlText = API_PLAYURL + '?' + API_PLAYURL_BVID + '=' + bvid
			+ '&' + API_PLAYURL_CID + '=' + cid
			+ '&' + API_PLAYURL_GAIA_SOURCE + '=' + gaia_source;
		URL url = new URL(urlText);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try (InputStream input = conn.getInputStream())
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			input.transferTo(bos);
			return bos.toString(StandardCharsets.UTF_8);
		}
		finally
		{
			conn.disconnect();
		}
		return urlText;
	}

	public static File video(String bvid, String downloadUrl, File downloadTo) throws Throwable
	{
		Map<String, String> headers = Map.of(
			HEADER_USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
			HEADER_REFERER, "https://www.bilibili.com/video/" + bvid + '/'
		);
		return HTTPAPI.download(downloadUrl, headers, downloadTo);
	}
}
