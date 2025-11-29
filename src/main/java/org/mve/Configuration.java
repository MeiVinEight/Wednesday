package org.mve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;

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
	private static final String KEY_LANGUAGE = "Language";
	private static final String KEY_OWNER = "Owner";
	private static final String KEY_COMMAND_PREFIX = "CommandPrefix";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final WednesdayLogger LOGGER = LoggerManager.create("Configuration");

	public static final String ONEBOT_WS_FORWARD;
	public static final String ONEBOT_TOKEN;
	public static final SimpleLogger.LogPriority LOG_LEVEL;
	public static final String LANGUAGE;
	public static final long OWNER;
	public static final String COMMAND_PREFIX;

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
			config.addProperty(KEY_LANGUAGE, LANGUAGE);
			config.addProperty(KEY_OWNER, OWNER);
			config.addProperty(KEY_COMMAND_PREFIX, COMMAND_PREFIX);
			fos.write(GSON.toJson(config).getBytes(StandardCharsets.UTF_8));
			fos.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("Configuration saving fail", e);
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
		String language = "zh_cn";
		long owner = 0;
		String commandPrefix = "/";

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
				if (config.has(KEY_LANGUAGE))
					language = config.get(KEY_LANGUAGE).getAsString();
				if (config.has(KEY_OWNER))
					owner = config.get(KEY_OWNER).getAsLong();
				if (config.has(KEY_COMMAND_PREFIX))
					commandPrefix = config.get(KEY_COMMAND_PREFIX).getAsString();
			}
			catch (IOException e)
			{
				LOGGER.error("Configuration loading fail", e);
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
			LOGGER.error("Wrong log level {}, alternative:{DEBUG, VERBOSE, INFO, WARNING, ERROR}", logLevel, e);
			logPriority = SimpleLogger.LogPriority.INFO;
		}
		LOG_LEVEL = logPriority;
		LANGUAGE = language;
		OWNER = owner;
		COMMAND_PREFIX = commandPrefix;

		if (newFile)
		{
			LOGGER.info("Configuration creating default");
			Configuration.save();
		}
	}
}
