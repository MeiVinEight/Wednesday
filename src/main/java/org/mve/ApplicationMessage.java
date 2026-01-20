package org.mve;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.ShortVideo;
import net.mamoe.mirai.utils.ExternalResource;
import org.mve.api.BilibiliAPI;
import org.mve.api.HTTPAPI;
import org.mve.uni.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApplicationMessage
{
	public static final String APP_BILIBILI = "1109937557";

	public static void application(MessageEvent event)
	{
		Contact subject = event.getSubject();
		for (Message singleMessage : event.getMessage())
		{
			if (!(singleMessage instanceof LightApp app))
				continue;
			ApplicationMessage.application(subject, app);
		}
	}

	public static void application(Contact subject, LightApp app)
	{
		Json content = Json.resolve(app.getContent());
		Json meta = content.get("meta");
		Json detail1 = meta.get("detail_1");
		if (!APP_BILIBILI.equals(detail1.string("appid")))
			return;
		String url = detail1.string("qqdocurl");
		subject.sendMessage(url);

		int idx = url.indexOf('?');
		if (idx != -1)
			url = url.substring(0, idx);

		Json view;

		try
		{
			view = Json.resolve(BilibiliAPI.view(url));
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.warn(null, e);
			subject.sendMessage("获取视频信息失败: " + e);
			return;
		}

		int code = view.number("code").intValue();
		String message = view.string("message");
		if (code != 0)
		{
			String msg = String.valueOf(code);
			if (message != null && !message.isEmpty())
				msg += ": " + message;
			subject.sendMessage(msg);
			return;
		}

		Json data = view.get("data");
		Json stat = data.get("stat");
		String bvid = data.string("bvid");
		String pic = data.string("pic");
		String dynamic = data.string("dynamic");
		long cid = data.number("cid").longValue();

		String summary = data.string("title") + '\n' +
			"UP: " + data.get("owner").string("name") + '\n' +
			"播放: " + stat.number("view") + '\n' +
			"弹幕: " + stat.number("danmaku") + '\n' +
			"点赞: " + stat.number("like") + '\n' +
			"投币: " + stat.number("coin") + '\n' +
			"收藏: " + stat.number("favorite") + '\n' +
			"分享: " + stat.number("share") + '\n' +
			"评论: " + stat.number("reply") + '\n' +
			"简介: " + data.string("desc");
		if (dynamic != null && !dynamic.isEmpty())
			summary += "\n动态: " + dynamic;
		subject.sendMessage(summary);

		File picFile = new File("tmp/" + bvid + ".jpg");
		try
		{
			picFile = HTTPAPI.download(pic, null, picFile);
		}
		catch (IOException e)
		{
			Wednesday.LOGGER.warn(null, e);
			subject.sendMessage("下载视频封面失败: " + e);
			return;
		}
		subject.sendMessage(subject.uploadImage(ExternalResource.create(picFile)));

		Json playurl;
		try
		{
			playurl = Json.resolve(BilibiliAPI.playurl(bvid, cid));
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.warn(null, e);
			subject.sendMessage("获取视频下载链接失败: " + e);
			return;
		}
		code = playurl.number("code").intValue();
		message = playurl.string("message");
		if (code != 0)
		{
			String msg = String.valueOf(code);
			if (message != null && !message.isEmpty())
				msg += ": " + message;
			subject.sendMessage(msg);
			return;
		}

		data = playurl.get("data");
		Json durl = data.get("durl");
		Json durl0 = durl.get(0);
		url = durl0.string("url");
		long size = durl0.number("size").longValue();
		if (size > Configuration.VIDEO_MAX_SIZE)
		{
			subject.sendMessage("视频太大了, 还是去b站看吧");
			return;
		}

		String uploadFileName = bvid + ".mp4";
		String uploadDir = "tmp";
		String uploadHttpURL = Configuration.COFFEE_SERVER + "/upload";
		Json requestHeader = new Json();
		requestHeader.set(BilibiliAPI.HEADER_USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
		requestHeader.set(BilibiliAPI.HEADER_REFERER, "https://www.bilibili.com/video/" + bvid + '/');

		Json body;
		try
		{
			URL uploadURL = new URL(uploadHttpURL);
			HttpURLConnection conn = (HttpURLConnection) uploadURL.openConnection();
			conn.setRequestProperty("Download-From", url);
			conn.setRequestProperty("Download-To", uploadDir + '/' + uploadFileName);
			conn.setRequestProperty("Download-Headers", requestHeader.stringify());
			code = conn.getResponseCode();
			if (code != 200)
			{
				Wednesday.LOGGER.warn("{}: {}", uploadHttpURL, conn.getResponseMessage());
				subject.sendMessage("下载视频失败: " + conn.getResponseMessage());
				return;
			}
			body = Json.resolve(new String(HTTPAPI.body(conn), StandardCharsets.UTF_8));
		}
		catch (IOException e)
		{
			Wednesday.LOGGER.warn(null, e);
			subject.sendMessage("下载视频失败: " + e);
			return;
		}

		code = body.number("code").intValue();
		if (code != 0 && code != 5)
		{
			Wednesday.LOGGER.warn(body.stringify());
			subject.sendMessage("下载视频失败\n" + code + ": " + body.string("message"));
			return;
		}

		ShortVideo video;
		try
		{
			video = ResourceManager.video(uploadDir + '/' + uploadFileName, uploadFileName, null);
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.warning(e);
			subject.sendMessage("上传视频失败: " + e);
			return;
		}

		subject.sendMessage(video);
	}
}
