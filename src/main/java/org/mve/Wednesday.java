package org.mve;

public class Wednesday extends Synchronize
{
	private final SynchronizeNET synchronize = new SynchronizeNET();

	public Wednesday()
	{
		this.synchronize.offer(this);
		new Thread(this.synchronize).start();
	}

	public void close()
	{
		this.cancel();
		this.synchronize.close();
	}

	@Override
	public void run()
	{
	}
}
