package org.mve.mixin;

import org.mve.woden.ResourceEnumeration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings("all")
public class MixinEngine extends URLClassLoader
{
	private static final MethodHandle ensureInitialization;
	private static final MethodHandle getAndVerifyPackage;
	private final Map<Object, ProtectionDomain> domain = new ConcurrentHashMap<>();
	private Object[][] classpath;
	BiFunction<ClassNode, String, AnnotationNode> getAnnotation = (classNode, annoName) ->
	{
		if (classNode.visibleAnnotations == null)
			return null;
		for (AnnotationNode annoNode : classNode.visibleAnnotations)
			if (annoNode.desc.equals(annoName))
				return annoNode;
		return null;
	};
	private final Map<String, List<MixinInfo>> mixins = new ConcurrentHashMap<>();

	public MixinEngine(File[] urls, ClassLoader parent) throws Throwable
	{
		super(new URL[0], parent);
		Objects.requireNonNull(urls);
		this.classpath = new Object[urls.length][2];
		Map<JarFile, List<String>> classMixinMap = new HashMap<>();
		for (int i = 0; i < urls.length; i++)
		{
			File libFile = (File) (this.classpath[i][0] = urls[i].getAbsoluteFile());
			JarFile jarFile = new JarFile(libFile);
			JarEntry entry = jarFile.getJarEntry("mixin.json");
			if (entry == null)
				continue;
			Json mixinConfig;
			try (InputStream inputStream = jarFile.getInputStream(entry))
			{
				ByteArrayOutputStream bbuf = new ByteArrayOutputStream(inputStream.available());
				inputStream.transferTo(bbuf);
				Array array = new Array(bbuf.size());
				array.put(bbuf.toByteArray());
				mixinConfig = Json.resolve(array);
			}
			Json mixins = mixinConfig.get("mixins");
			for (int idx = 0; idx < mixins.length(); idx++)
				classMixinMap.computeIfAbsent(jarFile, jf -> new LinkedList<>())
					.add(mixins.string(idx));
		}
		classMixinMap.forEach((jarFile, classMixins) ->
		{
			for (String className : classMixins)
			{
				String classFile = className.replace('.', '/').concat(".class");
				JarEntry entry = jarFile.getJarEntry(classFile);
				if (entry == null)
					throw new RuntimeException("Mixin class Not Found: " + jarFile.getName() + "!/" + classFile);

				byte[] classBytes;
				try (InputStream inputStream = jarFile.getInputStream(entry))
				{
					ByteArrayOutputStream bbuf = new ByteArrayOutputStream(inputStream.available());
					inputStream.transferTo(bbuf);
					classBytes = bbuf.toByteArray();
				}
				catch (Throwable t)
				{
					MixinMirroring.sneaking(t);
					throw new RuntimeException(t);
				}
				ClassNode classNode = new ClassNode();
				new ClassReader(classBytes).accept(classNode, ClassReader.EXPAND_FRAMES);
				AnnotationNode anno = MixinEngine.annotation(classNode.visibleAnnotations, "Lorg/mve/mixin/Mixin;");
				if (anno == null)
					throw new RuntimeException("@Mixin Annotation Not Found: " + className);
				int annoArgNum = (anno.values == null) ? 0 : anno.values.size();
				Map<String, Object> annoArgs = new HashMap<>(annoArgNum);
				if (anno.values != null)
					for (int annoArgIdx = 0; annoArgIdx < annoArgNum; annoArgIdx += 2)
						annoArgs.put((String) anno.values.get(annoArgIdx), anno.values.get(annoArgIdx + 1));
				List<Type> listType = MixinMirroring.checkcast(annoArgs.get("value"));
				if (listType != null)
				{
					for (Type type : listType)
					{
						String mixinType = type.getInternalName();
						this.mixins.computeIfAbsent(mixinType, k -> new LinkedList<>())
							.add(new MixinInfo(classNode, mixinType));
					}
				}
				List<String> listName = MixinMirroring.checkcast(annoArgs.get("target"));
				if (listName != null)
				{
					for (String val : listName)
					{
						String mixinType = val.replace('.', '/');
						this.mixins.computeIfAbsent(mixinType, k -> new LinkedList<>())
							.add(new MixinInfo(classNode, mixinType));
					}
				}
			}
		});
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException
	{
		String path = name.replace('.', '/')
			.concat(".class");

		try
		{
			JarFile jarFile = null;
			JarEntry entry = null;
			int index = -1;
			for (int i = 0; i < this.classpath.length; i++)
			{
				if (this.classpath[i][1] == null)
					this.classpath[i][1] = new JarFile((File) this.classpath[i][0]);
				JarFile jarFile0 = (JarFile) this.classpath[i][1];
				JarEntry entry0 = jarFile0.getJarEntry(path);
				if (entry0 == null)
					continue;
				index = i;
				jarFile = jarFile0;
				entry = entry0;
				break;
			}
			if (index == -1)
				throw new ClassNotFoundException(name);

			URL url = ((File) this.classpath[index][0]).toURI().toURL();

			int i = name.lastIndexOf('.');
			if (i != -1)
			{
				String packageName = name.substring(0, i);
				ensureInitialization.invokeExact(jarFile);
				Manifest man = jarFile.getManifest();
				if ((Package) getAndVerifyPackage.invokeExact((URLClassLoader) this, packageName, man, url) != null)
				{
					try
					{
						if (man != null)
							this.definePackage(packageName, man, url);
						else
							this.definePackage(
								packageName,
								null,
								null,
								null,
								null,
								null,
								null,
								null
							);
					}
					catch (IllegalArgumentException t)
					{
						if ((Package) getAndVerifyPackage.invokeExact((URLClassLoader) this, packageName, man, url) == null)
							throw new AssertionError("Cannot find package " + packageName, t);
					}
				}
			}

			byte[] classData;
			try (InputStream in = jarFile.getInputStream(entry))
			{
				ByteArrayOutputStream bbuf = new ByteArrayOutputStream(in.available());
				in.transferTo(bbuf);
				classData = bbuf.toByteArray();
			}

			ClassNode node = new ClassNode();
			new ClassReader(classData).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			if (this.getAnnotation.apply(node, "Lorg/mve/mixin/Mixin;") != null)
				throw new RuntimeException("Loading class with @Mixin Annotation");

			List<MixinInfo> infos = this.mixins.get(name.replace('.', '/'));
			if (infos != null && !infos.isEmpty())
			{
				MixinClassWriter writer = new MixinClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, this);
				MixinClassVisitor mcv = new MixinClassVisitor(writer, infos.toArray(MixinInfo[]::new));
				ClassReader cr = new ClassReader(classData);
				cr.accept(mcv, ClassReader.SKIP_FRAMES);
				classData = writer.toByteArray();
				try (FileOutputStream tmp = new FileOutputStream(node.name.substring(node.name.lastIndexOf('/') + 1) + ".class"))
				{
					tmp.write(classData);
					tmp.flush();
				}
			}

			CodeSigner[] signers = entry.getCodeSigners();
			CodeSource cs = new CodeSource(url, signers);
			return this.defineClass(name, classData, 0, classData.length, cs);
		}
		catch (Throwable e)
		{
			throw new ClassNotFoundException(name, e);
		}
	}

	@Override
	public URL findResource(String name)
	{
		for (int i = 0; i < this.classpath.length; i++)
		{
			try
			{
				if (this.classpath[i][1] == null)
					this.classpath[i][1] = new JarFile((File) this.classpath[i][0]);
				JarFile jarFile = (JarFile) this.classpath[i][1];
				JarEntry entry = jarFile.getJarEntry(name);
				if (entry == null)
					continue;
				return new URL("jar:file:///" + jarFile.getName() + "!/" + entry.getName());
			}
			catch (IOException e)
			{
				MixinMirroring.sneaking(e);
				return null;
			}
		}
		return null;
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException
	{
		List<String> res = new LinkedList<>();
		for (int i = 0; i < this.classpath.length; i++)
		{
			try
			{
				if (this.classpath[i][1] == null)
					this.classpath[i][1] = new JarFile((File) this.classpath[i][0]);
				JarFile jarFile = (JarFile) this.classpath[i][1];
				JarEntry entry = jarFile.getJarEntry(name);
				if (entry == null)
					continue;
				res.add("jar:file:///" + jarFile.getName() + "!/" + entry.getName());
			}
			catch (IOException e)
			{
				MixinMirroring.sneaking(e);
				return null;
			}
		}
		return new ResourceEnumeration(res.toArray(String[]::new));
	}

	public static AnnotationNode annotation(List<AnnotationNode> annos, String annoName)
	{
		if (annos == null)
			return null;
		for (AnnotationNode annoNode : annos)
			if (annoNode.desc.equals(annoName))
				return annoNode;
		return null;
	}

	static
	{
		try
		{
			ensureInitialization = MixinMirroring.LOOKUP.findVirtual(JarFile.class, "ensureInitialization", MethodType.methodType(void.class));
			getAndVerifyPackage = MixinMirroring.LOOKUP.findVirtual(
				URLClassLoader.class,
				"getAndVerifyPackage",
				MethodType.methodType(Package.class, String.class, Manifest.class, URL.class)
			);
		}
		catch (Throwable e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
}
