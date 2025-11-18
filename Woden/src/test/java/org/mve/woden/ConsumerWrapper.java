package org.mve.woden;

import org.gradle.internal.jvm.GradleVersionNumberLoader;
import org.gradle.wrapper.Download;
import org.gradle.wrapper.GradleUserHomeLookup;
import org.gradle.wrapper.GradleWrapperMain;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.Logger;
import org.gradle.wrapper.PathAssembler;
import org.gradle.wrapper.SystemPropertiesHandler;
import org.gradle.wrapper.WrapperConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class ConsumerWrapper implements Consumer<String[]>
{
	public static final String DISTRIBUTION_URL_PROPERTY = "distributionUrl";
	public static final String DISTRIBUTION_BASE_PROPERTY = "distributionBase";
	public static final String DISTRIBUTION_PATH_PROPERTY = "distributionPath";
	public static final String DISTRIBUTION_SHA_256_SUM = "distributionSha256Sum";
	public static final String ZIP_STORE_BASE_PROPERTY = "zipStoreBase";
	public static final String ZIP_STORE_PATH_PROPERTY = "zipStorePath";

	@Override
	public void accept(String[] args)
	{
		if (!GradleWrapperLoader.gradle(this.getClass().getClassLoader()))
			return;

		try
		{
			File wraperJar = wrapperJar();
			File propertiesFile = wrapperProperties(wraperJar);
			File rootDir = rootDir(propertiesFile);

			File gradleUserHome = GradleUserHomeLookup.gradleUserHome();
			addSystemProperties(gradleUserHome, rootDir);
			Logger logger = logger();
			if (!propertiesFile.exists())
				throw new RuntimeException(String.format("Wrapper properties file '%s' does not exist.", propertiesFile));
			/*
			WrapperExecutor wrapperExecutor = new WrapperExecutor(propertiesFile, new Properties());
			*/
			WrapperConfiguration config = new WrapperConfiguration();
			Properties properties = new Properties();
			try
			{
				loadProperties(propertiesFile, properties);
				config.setDistribution(prepareDistributionUri(propertiesFile, properties));
				config.setDistributionBase(getProperty(propertiesFile, properties, DISTRIBUTION_BASE_PROPERTY, config.getDistributionBase()));
				config.setDistributionPath(getProperty(propertiesFile, properties, DISTRIBUTION_PATH_PROPERTY, config.getDistributionPath()));
				config.setDistributionSha256Sum(getProperty(propertiesFile, properties, DISTRIBUTION_SHA_256_SUM, config.getDistributionSha256Sum(), false));
				config.setZipBase(getProperty(propertiesFile, properties, ZIP_STORE_BASE_PROPERTY, config.getZipBase()));
				config.setZipPath(getProperty(propertiesFile, properties, ZIP_STORE_PATH_PROPERTY, config.getZipPath()));
			}
			catch (Exception e)
			{
				throw new RuntimeException(String.format("Could not load wrapper properties from '%s'.", propertiesFile), e);
			}


			/*
			wrapperExecutor.execute(
				args,
				new Install(
					logger,
					new Download(logger, "gradlew", "0"),
					new PathAssembler(gradleUserHome, rootDir)
				)
			);
			*/

			Install install = new Install(logger, new Download(logger, "gradlew", "0"), new PathAssembler(gradleUserHome, rootDir));
			File gradleHome = install.createDist(config);
			File libDir = new File(gradleHome, "lib");
			File pluginsDir = new File(libDir, "plugins");
			addURL(this.getClass().getClassLoader(), libDir);
			addURL(this.getClass().getClassLoader(), pluginsDir);

			String javaVersion = System.getProperty("java.specification.version");
			if (javaVersion.equals("1.6") || javaVersion.equals("1.7"))
			{
				String gradleVersion = GradleVersionNumberLoader.loadGradleVersionNumber();
				throw new UnsupportedClassVersionError("Gradle " + gradleVersion + " requires Java 1.8 or later to run. You are currently using Java " + javaVersion);
			}

			new LauncherMain().run(args);
		}
		catch (Throwable e)
		{
			Woden.exception(e);
		}
	}

	public static void addURL(ClassLoader loader, File dir) throws MalformedURLException
	{
		if (!dir.exists() || !dir.isDirectory())
			return;
		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (File file : files)
			((Consumer<URL>) loader).accept(file.toURI().toURL());
	}

	public static File wrapperJar()
	{
		URI location;
		try
		{
			location = GradleWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}

		if (!location.getScheme().equals("file"))
		{
			throw new RuntimeException(String.format("Cannot determine classpath for wrapper Jar from codebase '%s'.", location));
		}
		else
		{
			try
			{
				return Paths.get(location).toFile();
			}
			catch (NoClassDefFoundError var2)
			{
				return new File(location.getPath());
			}
		}
	}

	public static File wrapperProperties(File wrapperJar)
	{
		return new File(wrapperJar.getParent(), wrapperJar.getName().replaceFirst("\\.jar$", ".properties"));
	}

	public static File rootDir(File wrapperJar)
	{
		return wrapperJar.getParentFile().getParentFile().getParentFile();
	}

	public static void addSystemProperties(File gradleHome, File rootDir)
	{
		System.getProperties().putAll(SystemPropertiesHandler.getSystemProperties(new File(gradleHome, "gradle.properties")));
		System.getProperties().putAll(SystemPropertiesHandler.getSystemProperties(new File(rootDir, "gradle.properties")));
	}

	public static Logger logger()
	{
		return new Logger(false);
	}

	private static void loadProperties(File propertiesFile, Properties properties) throws IOException
	{
		InputStream inStream = new FileInputStream(propertiesFile);

		try (inStream)
		{
			properties.load(inStream);
		}
	}

	private static URI prepareDistributionUri(File propertiesFile, Properties properties) throws URISyntaxException
	{
		URI source = readDistroUrl(propertiesFile, properties);
		return source.getScheme() == null ? (new File(propertiesFile.getParentFile(), source.getSchemeSpecificPart())).toURI() : source;
	}

	private static URI readDistroUrl(File propertiesFile, Properties properties) throws URISyntaxException
	{
		if (properties.getProperty(DISTRIBUTION_URL_PROPERTY) == null)
		{
			reportMissingProperty(propertiesFile, DISTRIBUTION_URL_PROPERTY);
		}

		return new URI(Objects.requireNonNull(getProperty(propertiesFile, properties, DISTRIBUTION_URL_PROPERTY, null, true)));
	}

	private static String reportMissingProperty(File propertiesFile, String propertyName)
	{
		throw new RuntimeException(String.format("No value with key '%s' specified in wrapper properties file '%s'.", propertyName, propertiesFile));
	}

	private static String getProperty(File propertiesFile, Properties properties, String propertyName, String defaultValue, boolean required)
	{
		String value = properties.getProperty(propertyName);
		if (value != null)
		{
			return value;
		}
		else if (defaultValue != null)
		{
			return defaultValue;
		}
		else
		{
			return required ? reportMissingProperty(propertiesFile, propertyName) : null;
		}
	}

	private static String getProperty(File pf, Properties p, String propertyName, String defaultValue)
	{
		return getProperty(pf, p, propertyName, defaultValue, true);
	}
}
