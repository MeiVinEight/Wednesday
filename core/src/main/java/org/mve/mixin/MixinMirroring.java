package org.mve.mixin;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

@SuppressWarnings("all")
public class MixinMirroring
{
	public static final MethodHandles.Lookup LOOKUP;

	public static <T, V> T checkcast(V obj)
	{
		return (T) obj;
	}

	public static <T extends Throwable> void sneaking(Throwable t) throws T
	{
		throw (T) t;
	}

	static
	{
		try
		{
			Constructor<?> ctor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
			ctor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(MethodHandles.Lookup.class, ctor);
			LOOKUP = (MethodHandles.Lookup) ctor.newInstance(Object.class, null, -1);
		}
		catch (Exception e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
}
