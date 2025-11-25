package org.mve;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fusesource.jansi.Ansi;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoggerMessage
{
	public static final String KEY_LOG_WEDNESDAY_SHUTDOWN = "log.wednesday.shutdown";
	public static final String KEY_LOG_WEDNESDAY_STARTUP = "log.wednesday.startup";
	public static final String KEY_LOG_WEDNESDAY_STARTUP_PATCHING = "log.wednesday.startup.patching";
	public static final String KEY_LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL = "log.wednesday.startup.patching.log.level.fail";
	public static final String KEY_LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC = "log.wednesday.startup.patching.log.level.succ";
	public static final String KEY_LOG_WEDNESDAY_UPDATE_ERROR = "log.wednesday.update.error";
	public static final String KEY_LOG_WEDNESDAY_UPDATE_GIT_TIMEOUT = "log.wednesday.update.git.timeout";

	public static final String LOG_WEDNESDAY_SHUTDOWN;
	public static final String LOG_WEDNESDAY_STARTUP;
	public static final String LOG_WEDNESDAY_STARTUP_PATCHING;
	public static final String LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL;
	public static final String LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC;
	public static final String LOG_WEDNESDAY_UPDATE_ERROR;
	public static final String LOG_WEDNESDAY_UPDATE_GIT_TIMEOUT;

	private static final char ESCAPING = '§';
	private static final WednesdayLogger LOGGER = LoggerManager.create("Language");
	private static final JsonObject LANGUAGE;

	public static String translate(String key)
	{
		if (LANGUAGE == null)
			return key;
		if (!LANGUAGE.has(key))
			return key;
		try
		{
			String val = LANGUAGE.get(key).getAsString();

			Ansi ansi = Ansi.ansi();
			boolean escaping = false;
			for (char c : val.toCharArray())
			{
				if (!escaping && ESCAPING == c)
				{
					escaping = true;
					continue;
				}

				if (!escaping)
				{
					ansi.a(c);
					continue;
				}

				// Escaping
				switch (c)
				{
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
						ansi.fg(Ansi.Color.values()[c - '0']);
						break;
					case '8':
					case '9':
						ansi.fgBright(Ansi.Color.values()[c - '8']);
						break;
					case 'a':
					case 'b':
					case 'c':
					case 'd':
					case 'e':
					case 'f':
						ansi.fgBright(Ansi.Color.values()[c - 'a']);
						break;
					case 'l':
						ansi.bold();
						break;
					case 'm':
						ansi.a("\u001B[9m");
						break;
					case 'n':
						ansi.a("\u001B[4m");
						break;
					case 'o':
						ansi.a("\u001b[3m");
						break;
					case 'r':
						ansi.reset();
						break;
					case ESCAPING:
						ansi.a(ESCAPING);
						break;
					default:
						ansi.a(ESCAPING).a(c);
						break;
				}
				escaping = false;
			}
			if (escaping)
				ansi.a(ESCAPING);
			key = ansi.toString();
		}
		catch (Throwable e)
		{
			LOGGER.error("Translation error", e);
		}
		return key;
	}

	static
	{
		LOGGER.info("Language {} loading", Configuration.LANGUAGE);
		JsonObject lang = null;
		LOADING:
		try
		{
			boolean found = false;
			TRY1:
			try (InputStream in = LoggerMessage.class.getClassLoader().getResourceAsStream(Configuration.LANGUAGE + ".json"))
			{
				if (in == null)
					break TRY1;
				found = true;
				lang = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
			}
			if (found)
				break LOADING;
			try (FileInputStream in = new FileInputStream("zh_cn.json"))
			{
				lang = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
			}
		}
		catch (Throwable e)
		{
			LOGGER.error("Language {} loading fail", Configuration.LANGUAGE, e);
		}
		LANGUAGE = lang;

		LOG_WEDNESDAY_SHUTDOWN               = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_SHUTDOWN);
		LOG_WEDNESDAY_STARTUP                = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_STARTUP);
		LOG_WEDNESDAY_STARTUP_PATCHING       = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_STARTUP_PATCHING);
		LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_PATCHING_LOGLEVEL_FAIL);
		LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_PATCHING_LOGLEVEL_SUCC);
		LOG_WEDNESDAY_UPDATE_ERROR           = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_UPDATE_ERROR);
		LOG_WEDNESDAY_UPDATE_GIT_TIMEOUT     = LoggerMessage.translate(LoggerMessage.KEY_LOG_WEDNESDAY_UPDATE_GIT_TIMEOUT);
	}
}
