package org.mve.woden;

import org.mve.mixin.MixinEngine;
import org.mve.mixin.MixinMirroring;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.function.Consumer;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			MethodHandle ucp = MixinMirroring.LOOKUP.findGetter(
				Class.forName("jdk.internal.loader.BuiltinClassLoader"),
				"ucp",
				Class.forName("jdk.internal.loader.URLClassPath")
			);
			MethodHandle addURL = MixinMirroring.LOOKUP.findVirtual(
				Class.forName("jdk.internal.loader.URLClassPath"),
				"addURL",
				MethodType.methodType(void.class, URL.class)
			);
			addURL.invoke(ucp.invoke(Main.class.getClassLoader()), new File("libs/asm-9.9.jar").toURI().toURL());
			addURL.invoke(ucp.invoke(Main.class.getClassLoader()), new File("libs/asm-tree-9.9.jar").toURI().toURL());

			File libDir = new File("libs");
			File[] libFiles = libDir.listFiles();
			MixinEngine loader = new MixinEngine(libFiles, MixinEngine.class.getClassLoader());

			Class<?> main = loader.loadClass("org.mve.Main");
			Constructor<?> ctor = main.getConstructor();
			MixinMirroring.<Consumer<String[]>, Object>checkcast(ctor.newInstance())
				.accept(args);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
