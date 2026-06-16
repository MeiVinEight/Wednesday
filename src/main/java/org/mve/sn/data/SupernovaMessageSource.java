package org.mve.sn.data;

import kotlin.Lazy;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSourceKind;
import net.mamoe.mirai.message.data.OfflineMessageSource;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class SupernovaMessageSource extends OfflineMessageSource
{
	public final Supernova context;
	public final Lazy<Json> message;
	public final Lazy<MessageSourceKind> kind;
	public final Lazy<Long> sender;
	public final Lazy<Long> target;
	public final Lazy<int[]> MID;
	public final Lazy<int[]> IID;
	public final Lazy<Integer> time;
	public final MessageChain original;

	public SupernovaMessageSource(Supernova context, MessageSourceKind kind, long sender, long target, int[] id, int[] iid, int time, MessageChain original)
	{
		this.context = context;
		this.message = new LazyJVM<>(() -> null);
		this.kind = new LazyJVM<>(() -> kind);
		this.sender = new LazyJVM<>(() -> sender);
		this.target = new LazyJVM<>(() -> target);
		MID = new LazyJVM<>(() -> id);
		IID = new LazyJVM<>(() -> iid);
		this.time = new LazyJVM<>(() -> time);
		this.original = original;
	}

	@NotNull
	@Override
	public MessageSourceKind getKind()
	{
		return this.kind.getValue();
	}

	@Override
	public long getBotId()
	{
		return this.context.ID;
	}

	@NotNull
	@Override
	public int[] getIds()
	{
		return this.MID.getValue();
	}

	@NotNull
	@Override
	public int[] getInternalIds()
	{
		return this.IID.getValue();
	}

	@Override
	public int getTime()
	{
		return this.time.getValue();
	}

	@Override
	public long getFromId()
	{
		return this.sender.getValue();
	}

	@Override
	public long getTargetId()
	{
		return this.target.getValue();
	}

	@NotNull
	@Override
	public MessageChain getOriginalMessage()
	{
		return this.original;
	}

	@Override
	public boolean isOriginalMessageInitialized()
	{
		return this.original != null;
	}
}
