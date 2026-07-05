package org.mve.orange.data;

import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSourceKind;
import net.mamoe.mirai.message.data.OfflineMessageSource;
import org.jetbrains.annotations.NotNull;
import org.mve.orange.core.Orange;

public class SourceOffline extends OfflineMessageSource
{
	public final Orange context;
	public final int ID;

	public SourceOffline(Orange context, int id)
	{
		this.context = context;
		ID = id;
	}

	@NotNull
	@Override
	public MessageSourceKind getKind()
	{
		return MessageSourceKind.TEMP;
	}

	@Override
	public long getBotId()
	{
		return this.context.getId();
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
		return 0;
	}

	@Override
	public long getFromId()
	{
		return 0;
	}

	@Override
	public long getTargetId()
	{
		return 0;
	}

	@NotNull
	@Override
	public MessageChain getOriginalMessage()
	{
		return new MessageChainBuilder().build();
	}

	@Override
	public boolean isOriginalMessageInitialized()
	{
		return false;
	}
}
