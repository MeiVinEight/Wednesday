package org.mve.service;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.mve.Configuration;
import org.mve.Database;
import org.mve.Json;
import org.mve.SimpleMapper;
import org.mve.Wednesday;
import org.mve.vo.Facing;
import top.mrxiaom.overflow.internal.message.data.WrappedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NudgeFacing
{
	private static final SimpleMapper<Facing> FACING;

	public static void capture(MessageEvent messageEvent)
	{
		MessageChain chain = messageEvent.getMessage();
		for (SingleMessage msg : chain)
		{
			if (!(msg instanceof WrappedImage wimg))
				continue;

			if (wimg.getJson() == null)
				continue;
			Json data = Json.resolve(wimg.getJson());
			Wednesday.LOGGER.verbose(data.stringify());
			String summary = data.string("summary");
			if (summary == null || summary.isEmpty())
				continue;

			String fileName = data.string("file");
			Facing fac = new Facing();
			fac.NAME = fileName;
			if (FACING.count(fac) > 0)
				continue;

			String url = data.string("url");
			try
			{
				String urlText = Configuration.COFFEE_SERVER + "/upload";
				URL uploadUrl = new URL(urlText);
				HttpURLConnection conn = (HttpURLConnection) uploadUrl.openConnection();
				conn.setRequestProperty("Download-From", url);
				conn.setRequestProperty("Download-To", "image/" + fileName);

				int respCode = conn.getResponseCode();
				if (respCode != HttpURLConnection.HTTP_OK)
				{
					Wednesday.LOGGER.error("{}: {}", urlText, conn.getResponseMessage());
					continue;
				}

				Json body;
				try (InputStream in = conn.getInputStream())
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					in.transferTo(out);
					body = Json.resolve(out.toString());
				}

				respCode = body.number("code").intValue();
				String message = body.string("message");
				if (respCode != 0 && respCode != 5)
				{
					Wednesday.LOGGER.warn("{}: {}", respCode, message);
					continue;
				}

				data = body.get("data");
				fac.SHA1 = data.string("SHA1");
				FACING.insert(fac);

			}
			catch (IOException e)
			{
				Wednesday.LOGGER.error(e);
			}
		}
	}

	static
	{
		String mysqlUrl = "jdbc:mysql://" + Configuration.MYSQL_HOST + ':' + Configuration.MYSQL_PORT + "/COFFEE";
		FACING = new SimpleMapper<>(new Database(mysqlUrl, Configuration.MYSQL_USERNAME, Configuration.MYSQL_PASSWORD));
	}
}
