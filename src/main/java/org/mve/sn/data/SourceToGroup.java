package org.mve.sn.data;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.core.Supernova;

public class SourceToGroup extends OnlineMessageSource.Outgoing.ToGroup
{
	public final Supernova context;
	private final Group group;
	private final int ID;
	private final int time;
	private final MessageChain message;

	public SourceToGroup(Supernova context, Group group, int id, int time, MessageChain message)
	{
		this.context = context;
		this.group = group;
		ID = id;
		this.time = time;
		this.message = message;
	}

	@NotNull
	@Override
	public Group getTarget()
	{
		return this.group;
	}

	@NotNull
	@Override
	public Bot getSender()
	{
		return this.context;
	}

	@NotNull
	@Override
	public Bot getBot()
	{
		return this.context;
	}

	@NotNull
	@Override
	public int[] getIds()
	{
		return new int[]{this.ID};
	}

	@NotNull
	@Override
	public int[] getInternalIds()
	{
		return new int[]{this.ID};
	}

	@Override
	public int getTime()
	{
		return this.time;
	}

	@NotNull
	@Override
	public MessageChain getOriginalMessage()
	{
		return this.message;
	}

	@Override
	public boolean isOriginalMessageInitialized()
	{
		return true;
	}
}
