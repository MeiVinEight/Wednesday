package org.mve.woden;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Woden
{
	public static final int STAT_RUNNING    = 1;
	public static final int STAT_UPDATE     = 2;
	public static final int STAT_TERMINATED = 3;
	public static final int OS_WINDOWS = 0;
	public static final int OS_LINUX   = 1;
	public static final int OS_UNKNOWN = 3;

	public static final int OS_TYPE;
	public static int stats = 0;
	public static final Object WAIT = new Object();

	public static void main(String[] args)
	{
		stats = STAT_RUNNING;
		while (stats < STAT_TERMINATED)
		{
			switch (stats)
			{
				case STAT_UPDATE:
				{
					try
					{
						pull();
						build();
					}
					catch (Throwable e)
					{
						e.printStackTrace(System.out);
						return;
					}
				}
				case STAT_RUNNING:
				{
					stats = STAT_TERMINATED;
					try
					{
						load();
					}
					catch (Throwable e)
					{
						e.printStackTrace(System.out);
					}
				}
			}
		}
	}

	private static void load() throws Throwable
	{
		File file = new File("libs");
		URL[] urls = Stream
			.of(Objects.requireNonNull(file.listFiles()))
			.map(Woden::toURL)
			.filter(Objects::nonNull)
			.toArray(URL[]::new);
		URLClassLoader loader = new URLClassLoader("Wednesday", urls, Woden.class.getClassLoader());
		Class<?> clazz = loader.loadClass("org.mve.Main");
		Method method = clazz.getDeclaredMethod("main", String[].class);
		method.setAccessible(true);
		method.invoke(null, (Object) new String[0]);
		loader.close();
	}

	private static URL toURL(File f)
	{
		try
		{
			return f.toURI().toURL();
		}
		catch (Throwable e)
		{
			exception(e);
		}
		return null;
	}

	public static Process exec(String cmd, String args) throws IOException
	{
		return new ProcessBuilder(cmd, args)
			.directory(new File("."))
			.inheritIO()
			.start();
	}

	public static void pull() throws Exception
	{
		Process proc = exec("git", "pull");
		proc.waitFor(1, TimeUnit.MINUTES);
	}

	public static void build() throws Exception
	{
		String gradlew = "gradlew";
		if (OS_TYPE == OS_WINDOWS)
			gradlew += ".bat";
		else if (OS_TYPE == OS_LINUX)
			gradlew = "./" + gradlew;
		Process proc = exec(gradlew, "build");
		proc.waitFor(1, TimeUnit.MINUTES);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void exception(Throwable t) throws T
	{
		throw (T) t;
	}

	static
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("win"))
			OS_TYPE = OS_WINDOWS;
		else if (osName.startsWith("linux"))
			OS_TYPE = OS_LINUX;
		else
			OS_TYPE = OS_UNKNOWN;
	}
}
