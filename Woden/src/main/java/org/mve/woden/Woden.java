package org.mve.woden;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;

public class Woden
{
	public static boolean running = true;
	public static final Object WAIT = new Object();

	public static void main(String[] args)
	{
		while (running)
		{
			running = false;
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

	private static void load() throws Throwable
	{
		File file = new File("libs");
		URL[] urls = Stream
			.of(Objects.requireNonNull(file.listFiles()))
			.map(Woden::toURL)
			.filter(Objects::nonNull)
			.toArray(URL[]::new);
		URLClassLoader loader = new URLClassLoader("Woden", urls, Woden.class.getClassLoader());
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

	private static Process git(String cmd) throws IOException
	{
		return new ProcessBuilder("git", cmd)
			.directory(new File("."))
			.inheritIO()
			.start();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void exception(Throwable t) throws T
	{
		throw (T) t;
	}
}
