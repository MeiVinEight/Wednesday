package org.mve.sn.data;

import kotlin.Lazy;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactOrBot;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.OnlineMessageSource;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class SourceFromStranger extends OnlineMessageSource.Incoming.FromStranger
{
	private final Supernova context;
	private final String raw;
	private final Lazy<Json> data;
	private final Lazy<Stranger> sender;
	private final Lazy<Integer> ID;
	private final Lazy<Integer> time;

	public SourceFromStranger(Supernova context, String raw)
	{
		this.context = context;
		this.raw = raw;
		this.data = new LazyJVM<>(() -> Json.resolve(SourceFromStranger.this.raw));
		this.sender = new LazyJVM<>(() -> this.context.getStranger(this.data.getValue().get(SupernovaAPI.KEY_SENDER).number(SupernovaAPI.KEY_USER_ID).longValue()));
		this.ID = new LazyJVM<>(() -> this.data.getValue().number(SupernovaAPI.KEY_MESSAGE_ID).intValue());
		this.time = new LazyJVM<>(() -> this.data.getValue().number(SupernovaAPI.KEY_TIME).intValue());
	}

	@NotNull
	@Override
	public Stranger getSender()
	{
		return this.sender.getValue();
	}

	@NotNull
	@Override
	public Stranger getSubject()
	{
		return this.getSender();
	}

	@NotNull
	@Override
	public ContactOrBot getTarget()
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
		return new int[]{this.ID.getValue()};
	}

	@NotNull
	@Override
	public int[] getInternalIds()
	{
		return new int[]{this.ID.getValue()};
	}

	@Override
	public int getTime()
	{
		return this.time.getValue();
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
