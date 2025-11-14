package org.mve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.utils.SimpleLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Configuration
{
	private static final String CONFIG_FILE = "config.json";
	private static final String KEY_ONEBOT = "OneBot";
	private static final String KEY_ONEBOT_WS_FORWARD = "WebSocket-Forward";
	private static final String KEY_ONEBOT_TOKEN = "Token";
	private static final String KEY_LOGLEVEL = "LogLevel";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final WednesdayLogger LOGGER = new WednesdayLogger();

	public static final String ONEBOT_WS_FORWARD;
	public static final String ONEBOT_TOKEN;
	public static final SimpleLogger.LogPriority LOG_LEVEL;


	public static void save()
	{
		try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
		{
			JsonObject configOnebot = new JsonObject();
			configOnebot.addProperty(KEY_ONEBOT_WS_FORWARD, ONEBOT_WS_FORWARD);
			configOnebot.addProperty(KEY_ONEBOT_TOKEN, ONEBOT_TOKEN);
			JsonObject config = new JsonObject();
			config.add(KEY_ONEBOT, configOnebot);
			config.addProperty(KEY_LOGLEVEL, LOG_LEVEL.toString());
			fos.write(GSON.toJson(config).getBytes(StandardCharsets.UTF_8));
			fos.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("保存配置文件失败", e);
		}
	}

	static
	{
		File configFile = new File(CONFIG_FILE);
		boolean newFile = !configFile.exists();

		// default configs
		String onebotWsForward = "ws://127.0.0.1:3001";
		String onebotToken = "";
		String logLevel = "INFO";

		if (!newFile)
		{
			try (FileReader reader = new FileReader(configFile))
			{
				JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
				JsonObject configOnebot = config.getAsJsonObject(KEY_ONEBOT);
				onebotWsForward = configOnebot.get(KEY_ONEBOT_WS_FORWARD).getAsString();
				if (configOnebot.has(KEY_ONEBOT_TOKEN))
					onebotToken = configOnebot.get(KEY_ONEBOT_TOKEN).getAsString();
				if (config.has(KEY_LOGLEVEL))
					logLevel = config.get(KEY_LOGLEVEL).getAsString();
			}
			catch (IOException e)
			{
				LOGGER.error("加载配置文件失败", e);
			}
		}

		ONEBOT_WS_FORWARD = onebotWsForward;
		ONEBOT_TOKEN = onebotToken;
		SimpleLogger.LogPriority logPriority;
		try
		{
			logPriority = SimpleLogger.LogPriority.valueOf(logLevel);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.error("未知的日志等级:{}, 使用默认配置", logLevel, e);
			logPriority = SimpleLogger.LogPriority.INFO;
		}
		LOG_LEVEL = logPriority;

		if (newFile)
		{
			LOGGER.info("创建默认配置");
			Configuration.save();
		}
	}
}
