package org.mve.uni;

import kotlin.Lazy;

import java.util.function.Supplier;

public class LazyJVM<T> implements Lazy<T>
{
	private final Supplier<T> supplier;
	private Object value;

	public LazyJVM(Supplier<T> supplier)
	{
		this.supplier = supplier;
	}

	@Override
	public T getValue()
	{
		if (!this.isInitialized())
			this.value = this.supplier.get();
		return Mirroring.checkcast(this.value);
	}

	@Override
	public boolean isInitialized()
	{
		return this.value != null;
	}

	public boolean setValue(T value)
	{
		if (this.isInitialized())
			return false;
		this.value = value;
		return true;
	}
}
