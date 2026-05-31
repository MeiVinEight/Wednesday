package org.mve.uni;

public class StackingMemory
{
	public final int ID;
	public final int JID;
	public final long address;
	public final long length;
	public final long current;

	public StackingMemory(int id, int jid, long address, long length, long current)
	{
		this.ID = id;
		this.JID = jid;
		this.address = address;
		this.length = length;
		this.current = current;
	}
}
