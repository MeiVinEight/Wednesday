package org.mve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.mamoe.mirai.utils.SimpleLogger;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	public static final Json JSON;

	public static SimpleLogger.LogPriority level()
	{
		return SimpleLogger.LogPriority.valueOf(Configuration.JSON.string(Configuration.KEY_LOGLEVEL));
	}

	public static void level(SimpleLogger.LogPriority level)
	{
		Configuration.JSON.set(Configuration.KEY_LOGLEVEL, level.toString());
		Configuration.saving();
	}

	public static String address()
	{
		return Configuration.JSON.string(Configuration.KEY_ADDR);
	}

	public static void address(String address)
	{
		Configuration.JSON.set(Configuration.KEY_ADDR, address);
		Configuration.saving();
	}

	public static int port()
	{
		return Configuration.JSON.number(Configuration.KEY_PORT).intValue();
	}

	public static void port(int port)
	{
		Configuration.JSON.set(Configuration.KEY_PORT, port);
		Configuration.saving();
	}

	public static void saving()
	{
		try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
		{
			fos.write(GSON.toJson(JsonParser.parseString(JSON.toString())).getBytes(StandardCharsets.UTF_8));
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
		Json json = new Json()
			.set(KEY_LOGLEVEL, SimpleLogger.LogPriority.INFO.toString())
			.set(KEY_ADDR, "127.0.0.1")
			.set(KEY_PORT, 8000);

		if (!newFile)
		{
			try (FileInputStream in = new FileInputStream(configFile))
			{
				json = Json.resolve(in);
			}
			catch (IOException e)
			{
				LOGGER.error("Configuration loading fail", e);
			}
		}
		JSON = json;

		try
		{
			Configuration.level();
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.error("Wrong log level {}, alternative:{DEBUG, VERBOSE, INFO, WARNING, ERROR}", Configuration.JSON.string(Configuration.KEY_LOGLEVEL), e);
		}

		if (newFile)
		{
			LOGGER.info("Configuration creating default");
			Configuration.saving();
		}
	}
}
