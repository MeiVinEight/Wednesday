package org.mve.uni;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unchecked")
public class Mirroring
{
	public static final MethodHandles.Lookup LOOKUP;
	public static final int FIELD_GETTER = 0;
	public static final int FIELD_SETTER = 1;
	private static final ConcurrentMap<ClassLoader, ConcurrentMap<Class<?>, ConcurrentMap<String, Object>>> MAPPING = new ConcurrentHashMap<>();

	public static <T> T get(Class<?> clazz, String fieldName)
	{
		try
		{
			return (T) Mirroring.mapping(clazz, fieldName, FIELD_GETTER).invoke();
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T get(Class<?> clazz, String fieldName, Object obj)
	{
		try
		{
			return (T) Mirroring.mapping(clazz, fieldName, FIELD_GETTER).invoke(obj);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static void set(Class<?> clazz, String fieldName, Object value)
	{
		try
		{
			Mirroring.mapping(clazz, fieldName, FIELD_SETTER).invoke(value);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static <T> void set(Class<?> clazz, String fieldName, Object obj, Object value)
	{
		try
		{
			Mirroring.mapping(clazz, fieldName, FIELD_SETTER).invoke(obj, value);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static MethodHandle mapping(Class<?> clazz, String fieldName, int type)
	{
		try
		{
			Field field = clazz.getDeclaredField(fieldName);
			String desc = MethodType.methodType(field.getType()).toMethodDescriptorString().substring(2);
			String key = fieldName + ':' + desc;
			ClassLoader cl = clazz.getClassLoader();
			if (cl == null)
				cl = ClassLoader.getSystemClassLoader();
			MethodHandle[] handles = (MethodHandle[]) MAPPING
				.computeIfAbsent(cl, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(key, k -> new MethodHandle[2]);
			if (handles[type] == null)
			{
				int flag = ((field.getModifiers() & Modifier.STATIC) >> 2) | type;
				switch (flag)
				{
					case 0: // VIRTUAL GETTER
					{
						handles[type] = LOOKUP.findGetter(clazz, fieldName, field.getType());
						break;
					}
					case 1: // VIRTUAL SETTER
					{
						handles[type] = LOOKUP.findSetter(clazz, fieldName, field.getType());
						break;
					}
					case 2: // STATIC GETTER
					{
						handles[type] = LOOKUP.findStaticGetter(clazz, fieldName, field.getType());
						break;
					}
					case 3: // STATIC SETTER
					{
						handles[type] = LOOKUP.findStaticSetter(clazz, fieldName, field.getType());
					}
				}
			}
			return handles[type];
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static <T extends Throwable> void thrown(Throwable t) throws T
	{
		throw (T) t;
	}

	static
	{
		try
		{
			Constructor<?> ctr = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
			ctr = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(MethodHandles.Lookup.class, ctr);
			LOOKUP = (MethodHandles.Lookup) ctr.newInstance(Object.class, null, -1);
		}
		catch(Throwable t)
		{
			Mirroring.thrown(t);
			throw new RuntimeException(t);
		}
	}
}
