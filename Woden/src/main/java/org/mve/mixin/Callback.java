package org.mve.mixin;

public class Callback
{
	public Object returning;
	public boolean cancelled = false;

	public void cancel()
	{
		this.cancelled = true;
	}

	public void returning(Object returning)
	{
		this.cancel();
		this.returning = returning;
	}
}
