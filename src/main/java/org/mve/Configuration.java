package org.mve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Configuration
{
	private static final String CONFIG_FILE = "config.json";
	private static final String KEY_ONEBOT = "onebot";
	private static final String KEY_ONEBOT_WS_FORWARD = "ws-forward";
	private static final String KEY_ONEBOT_TOKEN = "token";
	private static final String KEY_ONEBOT_RECONNECT = "reconnect-trys";
	private static final String KEY_LOGLEVEL = "log-level";
	private static final String KEY_LANGUAGE = "lang";
	private static final String KEY_OWNER = "owner";
	private static final String KEY_COMMAND_PREFIX = "command-prefix";
	private static final String KEY_MYSQL_HOST = "mysql-host";
	private static final String KEY_MYSQL_PORT = "mysql-port";
	private static final String KEY_MYSQL_USERNAME = "mysql-username";
	private static final String KEY_MYSQL_PASSWORD = "mysql-password";
	private static final String KEY_FILE_SERVER = "file-server";
	private static final String KEY_COFFEE_SERVER = "coffee-server";
	private static final String KEY_VIDEO_MAX_SIZE = "video-max-size";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final WednesdayLogger LOGGER = LoggerManager.create("Configuration");

	public static final String ONEBOT_WS_FORWARD;
	public static final String ONEBOT_TOKEN;
	public static final int ONEBOT_RECONNECT;
	public static final SimpleLogger.LogPriority LOG_LEVEL;
	public static final String LANGUAGE;
	public static final long OWNER;
	public static final String COMMAND_PREFIX;
	public static final String MYSQL_HOST;
	public static final int MYSQL_PORT;
	public static final String MYSQL_USERNAME;
	public static final String MYSQL_PASSWORD;
	public static final String FILE_SERVER;
	public static final String COFFEE_SERVER;
	public static final long VIDEO_MAX_SIZE;

	public static void save()
	{
		try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
		{
			JsonObject config = new JsonObject();
			JsonObject configOnebot = new JsonObject();
			configOnebot.addProperty(KEY_ONEBOT_WS_FORWARD, ONEBOT_WS_FORWARD);
			configOnebot.addProperty(KEY_ONEBOT_TOKEN, ONEBOT_TOKEN);
			configOnebot.addProperty(KEY_ONEBOT_RECONNECT, ONEBOT_RECONNECT);
			config.add(KEY_ONEBOT, configOnebot);

			config.addProperty(KEY_LOGLEVEL, LOG_LEVEL.toString());
			config.addProperty(KEY_LANGUAGE, LANGUAGE);
			config.addProperty(KEY_OWNER, OWNER);
			config.addProperty(KEY_COMMAND_PREFIX, COMMAND_PREFIX);
			config.addProperty(KEY_MYSQL_HOST, MYSQL_HOST);
			config.addProperty(KEY_MYSQL_PORT, MYSQL_PORT);
			config.addProperty(KEY_MYSQL_USERNAME, MYSQL_USERNAME);
			config.addProperty(KEY_MYSQL_PASSWORD, MYSQL_PASSWORD);
			config.addProperty(KEY_FILE_SERVER, FILE_SERVER);
			config.addProperty(KEY_COFFEE_SERVER, COFFEE_SERVER);
			config.addProperty(KEY_VIDEO_MAX_SIZE, VIDEO_MAX_SIZE);
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
		int onebotRetry = 5;
		String logLevel = "INFO";
		String language = "zh_cn";
		long owner = 0;
		String commandPrefix = "/";
		String mysqlHost = "127.0.0.1";
		int mysqlPort = 3306;
		String mysqlUsername = "root";
		String mysqlPassword = "root";
		String fileServer = ".";
		String coffeeServer = "http://127.0.0.1:8800";
		long videoMaxSize = 32 * 1024 * 1024;

		if (!newFile)
		{
			try (FileReader reader = new FileReader(configFile))
			{
				JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
				if (config.has(KEY_ONEBOT))
				{
					JsonObject configOnebot = config.getAsJsonObject(KEY_ONEBOT);
					if (configOnebot.has(KEY_ONEBOT_WS_FORWARD))
						onebotWsForward = configOnebot.get(KEY_ONEBOT_WS_FORWARD).getAsString();
					if (configOnebot.has(KEY_ONEBOT_TOKEN))
						onebotToken = configOnebot.get(KEY_ONEBOT_TOKEN).getAsString();
					if (configOnebot.has(KEY_ONEBOT_RECONNECT))
						onebotRetry = configOnebot.get(KEY_ONEBOT_RECONNECT).getAsInt();
				}
				if (config.has(KEY_LOGLEVEL))
					logLevel = config.get(KEY_LOGLEVEL).getAsString();
				if (config.has(KEY_LANGUAGE))
					language = config.get(KEY_LANGUAGE).getAsString();
				if (config.has(KEY_OWNER))
					owner = config.get(KEY_OWNER).getAsLong();
				if (config.has(KEY_COMMAND_PREFIX))
					commandPrefix = config.get(KEY_COMMAND_PREFIX).getAsString();
				if (config.has(KEY_MYSQL_HOST))
					mysqlHost = config.get(KEY_MYSQL_HOST).getAsString();
				if (config.has(KEY_MYSQL_PORT))
					mysqlPort = config.get(KEY_MYSQL_PORT).getAsInt();
				if (config.has(KEY_MYSQL_USERNAME))
					mysqlUsername = config.get(KEY_MYSQL_USERNAME).getAsString();
				if (config.has(KEY_MYSQL_PASSWORD))
					mysqlPassword = config.get(KEY_MYSQL_PASSWORD).getAsString();
				if (config.has(KEY_FILE_SERVER))
					fileServer = config.get(KEY_FILE_SERVER).getAsString();
				if (config.has(KEY_COFFEE_SERVER))
					coffeeServer = config.get(KEY_COFFEE_SERVER).getAsString();
				if (config.has(KEY_VIDEO_MAX_SIZE))
					videoMaxSize = config.get(KEY_VIDEO_MAX_SIZE).getAsLong();
				try
				{
					new URL(fileServer);
				}
				catch (MalformedURLException e)
				{
					Configuration.LOGGER.verbose(" Assume " + fileServer + " is file path");
					File filePath = new File(fileServer);
					if (!filePath.isDirectory())
						throw new FileNotFoundException(fileServer + " is not a directory");
					fileServer = "file:///" + fileServer;
					Configuration.LOGGER.verbose(fileServer);
				}
			}
			catch (IOException e)
			{
				LOGGER.error("Configuration loading fail", e);
			}
		}

		ONEBOT_WS_FORWARD = onebotWsForward;
		ONEBOT_TOKEN = onebotToken;
		ONEBOT_RECONNECT = onebotRetry;
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
		MYSQL_HOST = mysqlHost;
		MYSQL_PORT = mysqlPort;
		MYSQL_USERNAME = mysqlUsername;
		MYSQL_PASSWORD = mysqlPassword;
		FILE_SERVER = fileServer;
		COFFEE_SERVER = coffeeServer;
		VIDEO_MAX_SIZE = videoMaxSize;

		if (newFile)
		{
			LOGGER.info("Configuration creating default");
			Configuration.save();
		}
	}
}
