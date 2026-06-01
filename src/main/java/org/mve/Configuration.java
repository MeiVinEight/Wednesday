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
	private static final String KEY_LOGLEVEL = "log-level";
	private static final String KEY_ADDR = "address";
	private static final String KEY_PORT = "port";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final WednesdayLogger LOGGER = LoggerManager.create("Configuration");

	public static final String DATA_DIR = "data";
	public static final SimpleLogger.LogPriority LOG_LEVEL;
	public static final String ADDRESS;
	public static final int PORT;

	public static void save()
	{
	}

	static
	{
		File configFile = new File(CONFIG_FILE);
		boolean newFile = !configFile.exists();

		// default configs
		String logLevel = "INFO";
		String addr = "127.0.0.1";
		int port = 8000;

		if (!newFile)
		{
			try (FileReader reader = new FileReader(configFile))
			{
				JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
				if (config.has(KEY_LOGLEVEL))
					logLevel = config.get(KEY_LOGLEVEL).getAsString();
				if (config.has(KEY_ADDR))
					addr = config.get(KEY_ADDR).getAsString();
				if (config.has(KEY_PORT))
					port = config.get(KEY_PORT).getAsInt();
			}
			catch (IOException e)
			{
				LOGGER.error("Configuration loading fail", e);
			}
		}

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
		ADDRESS = addr;
		PORT = port;

		if (newFile)
		{
			LOGGER.info("Configuration creating default");
			try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
			{
				JsonObject config = new JsonObject();
				config.addProperty(KEY_LOGLEVEL, LOG_LEVEL.toString());
				config.addProperty(KEY_ADDR, ADDRESS);
				config.addProperty(KEY_PORT, PORT);
				fos.write(GSON.toJson(config).getBytes(StandardCharsets.UTF_8));
				fos.flush();
			}
			catch (IOException e)
			{
				LOGGER.error("Configuration saving fail", e);
			}
		}
	}
}
