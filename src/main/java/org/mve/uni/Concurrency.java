package org.mve.uni;

public class Concurrency
{
	public static void wait(Object obj, long ms, boolean interruptible)
	{
		if (ms == 0)
			return;
		long until = ms > 0 ? (ms + System.currentTimeMillis()) : Long.MAX_VALUE;
		while (true)
		{
			try
			{
				long now = System.currentTimeMillis();
				if (now < until)
					obj.wait(until - now);
				return;
			}
			catch (InterruptedException e)
			{
				if (interruptible)
					Mirroring.thrown(e);
			}
		}
	}
}
