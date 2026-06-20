package org.mve.mixin;

import java.util.Objects;

public class MethodRef
{
	public final String clazz;
	public final String name;
	public final String type;

	public MethodRef(String clazz, String name, String type)
	{
		this.clazz = clazz;
		this.name = name;
		this.type = type;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof MethodRef methodRef)) return false;
		return Objects.equals(clazz, methodRef.clazz) && Objects.equals(name, methodRef.name) && Objects.equals(type, methodRef.type);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(clazz, name, type);
	}
}
