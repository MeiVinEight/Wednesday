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

	public String nameAndType()
	{
		return this.name + this.type;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof MethodRef methodRef)) return false;
		return Objects.equals(this.clazz, methodRef.clazz) && Objects.equals(this.name, methodRef.name) && Objects.equals(this.type, methodRef.type);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.clazz, this.name, this.type);
	}

	@Override
	public String toString()
	{
		return this.clazz + "." + this.name + ":" + this.type;
	}
}
