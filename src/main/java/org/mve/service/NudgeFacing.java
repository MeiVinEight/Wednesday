package org.mve.service;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.mve.Configuration;
import org.mve.data.Database;
import org.mve.uni.Json;
import org.mve.data.SimpleMapper;
import org.mve.Wednesday;
import org.mve.vo.Facing;
import top.mrxiaom.overflow.internal.message.data.WrappedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class NudgeFacing
{
	private static final SimpleMapper<Facing> FACING;
	private static final Random RANDOM = new Random();

	public static void capture(MessageEvent messageEvent)
	{
		MessageChain chain = messageEvent.getMessage();
		for (SingleMessage msg : chain)
		{
			if (!(msg instanceof WrappedImage wimg))
				continue;
			capture(wimg);
		}
	}

	public static void capture(WrappedImage wimg)
	{
		if (wimg.getJson() == null)
			return;
		Json data = Json.resolve(wimg.getJson());
		Wednesday.LOGGER.debug(data.stringify());
		String summary = data.string("summary");
		if (summary == null || summary.isEmpty())
			return;

		String fileName = data.string("file");
		Facing fac = new Facing();
		fac.NAME = fileName;
		if (FACING.count(fac) > 0)
			return;

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
				return;
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
				return;
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

	public static void nudge(NudgeEvent nudge)
	{
		if (nudge.getTarget().getId() != nudge.getBot().getId())
			return;
		int count = FACING.count("FACING");
		Wednesday.LOGGER.debug("{} Facings", count);
		Facing fac = new Facing();
		fac.ID = RANDOM.nextInt(count);
		fac = FACING.primary(fac);
		String url = Configuration.FILE_SERVER + "/image/" + fac.NAME;
		Image image = Image.fromId(url);
		if (url.startsWith("file:///"))
		{
			File file = new File(url.substring(8));
			if (!file.isFile())
			{
				nudge.getSubject().sendMessage("File Not Found\nID=" + fac.ID + ", SHA1=" + fac.SHA1 + ", NAME=" + fac.NAME);
				return;
			}
			image = Contact.uploadImage(nudge.getSubject(), file);
		}
		nudge.getSubject().sendMessage(image);
	}

	static
	{
		String mysqlUrl = "jdbc:mysql://" + Configuration.MYSQL_HOST + ':' + Configuration.MYSQL_PORT + "/COFFEE";
		FACING = new SimpleMapper<>(new Database(mysqlUrl, Configuration.MYSQL_USERNAME, Configuration.MYSQL_PASSWORD));
	}
}
