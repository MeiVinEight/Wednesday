package org.mve.woden;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class GradleWrapperLoader extends URLClassLoader implements Consumer<URL>
{
	public GradleWrapperLoader(URL urls)
	{
		super(new URL[]{urls}, ClassLoader.getSystemClassLoader().getParent());
	}

	@Override
	public void addURL(URL url)
	{
		super.addURL(url);
	}

	@Override
	public void accept(URL url)
	{
		this.addURL(url);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		if (!name.equals("org.mve.woden.LauncherMain"))
			return super.findClass(name);
		try (InputStream in = GradleWrapperLoader.class.getResourceAsStream("/LauncherMain.class"))
		{
			Objects.requireNonNull(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			in.transferTo(out);
			out.flush();
			out.close();
			byte[] data = out.toByteArray();
			return defineClass(name, data, 0, data.length);
		}
		catch (Throwable e)
		{
			throw new ClassNotFoundException(name, e);
		}
	}

	public <T> Class<T> defining(byte[] data)
	{
		return (Class<T>) this.defineClass(null, data, 0, data.length, (ProtectionDomain) null);
	}

	public static boolean gradle(ClassLoader loader)
	{
		if (loader == null)
			return false;
		if (loader instanceof GradleWrapperLoader)
			return true;
		return loader.getClass().getName().equals(GradleWrapperLoader.class.getName());
	}
}
