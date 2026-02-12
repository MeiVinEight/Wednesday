package org.mve.uni;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unchecked")
public class Mirroring
{
	public static final MethodHandles.Lookup LOOKUP;
	public static final int FIELD_VIRTUAL_GETTER = 0;
	public static final int FIELD_VIRTUAL_SETTER = 1;
	public static final int FIELD_STATIC_GETTER = 2;
	public static final int FIELD_STATIC_SETTER = 3;
	private static final ConcurrentMap<ClassLoader, ConcurrentMap<Class<?>, ConcurrentMap<String, Object>>> MAPPING = new ConcurrentHashMap<>();

	public static <T> T get(Class<?> clazz, String fieldName)
	{
		try
		{
			return (T) Mirroring.mapping(clazz, fieldName, FIELD_STATIC_GETTER).invoke();
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T get(Class<?> clazz, String fieldName, Class<?> type)
	{
		try
		{
			return (T) Mirroring.mapping(clazz, fieldName, type, FIELD_STATIC_GETTER).invoke();
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
			return (T) Mirroring.mapping(clazz, fieldName, FIELD_VIRTUAL_GETTER).invoke(obj);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T get(Class<?> clazz, String fieldName, Class<?> type, Object obj)
	{
		try
		{
			return (T) Mirroring.mapping(clazz, fieldName, type, FIELD_VIRTUAL_GETTER).invoke(obj);
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
			Mirroring.mapping(clazz, fieldName, FIELD_STATIC_SETTER).invoke(value);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static void set(Class<?> clazz, String fieldName, Class<?> type, Object value)
	{
		try
		{
			Mirroring.mapping(clazz, fieldName, type, FIELD_STATIC_SETTER).invoke(value);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static void set(Class<?> clazz, String fieldName, Object obj, Object value)
	{
		try
		{
			Mirroring.mapping(clazz, fieldName, FIELD_VIRTUAL_SETTER).invoke(obj, value);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static void set(Class<?> clazz, String fieldName, Class<?> type, Object obj, Object value)
	{
		try
		{
			Mirroring.mapping(clazz, fieldName, type, FIELD_VIRTUAL_SETTER).invoke(obj, value);
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
			return mapping(clazz, fieldName, field.getType(), type);
		}
		catch (Throwable e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}

	public static MethodHandle mapping(Class<?> clazz, String fieldName, Class<?> type, int invokeType)
	{
		try
		{
			String desc = MethodType.methodType(type).toMethodDescriptorString().substring(2);
			String key = fieldName + ':' + desc;
			ClassLoader cl = clazz.getClassLoader();
			if (cl == null)
				cl = ClassLoader.getSystemClassLoader();
			MethodHandle[] handles = (MethodHandle[]) MAPPING
				.computeIfAbsent(cl, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(key, k -> new MethodHandle[2]);
			int gsType = invokeType & 1;
			if (handles[gsType] == null)
			{
				switch (invokeType)
				{
					case FIELD_VIRTUAL_GETTER: // VIRTUAL GETTER
					{
						handles[gsType] = LOOKUP.findGetter(clazz, fieldName, type);
						break;
					}
					case FIELD_VIRTUAL_SETTER: // VIRTUAL SETTER
					{
						handles[gsType] = LOOKUP.findSetter(clazz, fieldName, type);
						break;
					}
					case FIELD_STATIC_GETTER: // STATIC GETTER
					{
						handles[gsType] = LOOKUP.findStaticGetter(clazz, fieldName, type);
						break;
					}
					case FIELD_STATIC_SETTER: // STATIC SETTER
					{
						handles[gsType] = LOOKUP.findStaticSetter(clazz, fieldName, type);
					}
				}
			}
			return handles[gsType];
		}
		catch (Throwable t)
		{
			Mirroring.thrown(t);
			throw new RuntimeException(t);
		}
	}

	public static <T extends Throwable> void thrown(Throwable t) throws T
	{
		throw (T) t;
	}

	public static <T> T checkcast(Object obj)
	{
		return (T) obj;
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
