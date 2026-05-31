package org.mve.service;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import org.mve.data.Creation;
import org.mve.data.Database;
import org.mve.data.Query;
import org.mve.uni.Json;
import org.mve.data.SimpleMapper;
import org.mve.Wednesday;
import org.mve.vo.Facing;
import top.mrxiaom.overflow.internal.message.data.WrappedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class NudgeFacing
{
	private static final SimpleMapper<Facing> FACING;
	private static final Random RANDOM = new Random();
	private static final String IMAGE_DIR = "data/facing";

	public static void capture(MessageEvent event, WrappedImage wimg)
	{
		if (event.getSender().getId() == event.getBot().getId())
			return;
		if (wimg.getRawJson() == null)
			return;
		Json data = Json.resolve(wimg.getRawJson().toString());
		Wednesday.LOGGER.debug(data.stringify());
		String summary = data.string("summary");
		if (summary == null || summary.isEmpty())
			return;

		String fileName = data.string("file");
		Facing fac = new Facing();
		fac.NAME = fileName;
		if (FACING.count(fac) > 0)
			return;


		try
		{
			String urlTxt = data.string("url");
			if (urlTxt == null)
				return;

			URL url = new URL(urlTxt);
			URLConnection conn = url.openConnection();
			File tmpFile = new File(IMAGE_DIR, fileName);
			try (InputStream in = conn.getInputStream(); FileOutputStream tmpOut = new FileOutputStream(tmpFile))
			{
				in.transferTo(tmpOut);
			}

			fac.ID = FACING.count(Facing.TABLE);
			if (fac.ID < 0)
				return;
			/*
			String url = data.string("url");
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
			*/
			FACING.insert(fac);
		}
		catch (IOException e)
		{
			Wednesday.LOGGER.error(e);
			event.getSubject().sendMessage("表情下载失败: " + e.getLocalizedMessage());
		}
	}

	public static void nudge(NudgeEvent nudge)
	{
		if (nudge.getTarget().getId() != nudge.getBot().getId())
			return;
		int count = FACING.count(Facing.TABLE);
		if (count < 0)
			return;
		Wednesday.LOGGER.debug("{} Facings", count);
		Facing fac = new Facing();
		fac.ID = RANDOM.nextInt(count);
		fac = FACING.primary(fac);
		String url ="file:///" + IMAGE_DIR + "/" + fac.NAME;
		Image image = Image.fromId(url);
		if (url.startsWith("file:///"))
		{
			File file = new File(url.substring(8));
			if (!file.isFile())
			{
				nudge.getSubject().sendMessage("File Not Found\nID=" + fac.ID + ", SHA1=" + fac.NAME + ", NAME=" + fac.NAME);
				return;
			}
			image = Contact.uploadImage(nudge.getSubject(), file);
		}
		nudge.getSubject().sendMessage(image);
	}

	static
	{
		FACING = new SimpleMapper<>(new Database("jdbc:sqlite:data/FACING.DB", null, null), Facing.class);
		boolean created = new Creation(Facing.TABLE)
			.db(Query.TYPE_SQLITE)
			.column("ID", "INTEGER")
			.primary()
			.column("NAME", "VARCHAR(48)")
			.notnull()
			.unique()
			.query(FACING);
		if (created)
			Wednesday.LOGGER.info("创建表情包表 FACING");
		boolean ignored = new File(IMAGE_DIR).mkdirs();
	}
}
