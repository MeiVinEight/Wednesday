package org.mve.woden;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class GradleWrapper
{
	public static final GradleWrapperLoader GRADLE_WRAPPER_LOADER;
	public static final Consumer<String[]> GRADLEW;

	public static void gradle(String args)
	{
		if (GRADLEW == null)
			return;
		String[] argA = null;
		if (args != null)
			argA = args.split(" ");
		GRADLEW.accept(argA);
	}

	static
	{
		GradleWrapperLoader cl = null;
		Consumer<String[]> gradlew = null;
		CLINIT:
		{
			if (GradleWrapper.class.getClassLoader().getClass().getName().equals(GradleWrapperLoader.class.getName()))
				break CLINIT;
			File gradleWrapper = new File("gradle/wrapper/gradle-wrapper.jar");
			if (!gradleWrapper.exists())
			{
				System.out.println("未找到gradle-wrapper.jar, 热重载功能禁用");
				break CLINIT;
			}
			URL url;
			try
			{
				url = gradleWrapper.toURI().toURL();
			}
			catch (MalformedURLException e)
			{
				System.out.println("加载gradle-wrapper.jar失败, 热重载功能禁用");
				e.printStackTrace(System.out);
				break CLINIT;
			}
			cl = new GradleWrapperLoader(url);
			cl.addURL(GradleWrapper.class.getProtectionDomain().getCodeSource().getLocation());

			try (InputStream in = GradleWrapper.class.getResourceAsStream("/ConsumerWrapper.class"))
			{
				Objects.requireNonNull(in);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				in.transferTo(bout);
				bout.flush();
				bout.close();
				byte[] data = bout.toByteArray();
				Class<? extends Consumer<String[]>> clazz = cl.defining(data);
				Constructor<? extends Consumer<String[]>> constructor = clazz.getConstructor();
				gradlew = constructor.newInstance();
			}
			catch (Throwable e)
			{
				System.out.println("创建GradleWrapper失败, 热重载功能禁用");
				e.printStackTrace(System.out);
			}
		}
		GRADLE_WRAPPER_LOADER = cl;
		GRADLEW = gradlew;
	}
}
