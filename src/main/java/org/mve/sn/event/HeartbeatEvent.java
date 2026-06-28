package org.mve.sn.event;

import kotlin.Lazy;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.events.BotPassiveEvent;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class HeartbeatEvent extends AbstractEvent implements BotPassiveEvent
{
	public final Supernova context;
	public final Json data;
	public final Lazy<Boolean> online;
	public final Lazy<Boolean> good;

	public HeartbeatEvent(Supernova context, Json data)
	{
		this.context = context;
		this.data = data;
		this.online = new LazyJVM<>(() -> this.data.bool(SupernovaAPI.KEY_ONLINE));
		this.good = new LazyJVM<>(() -> this.data.bool(SupernovaAPI.KEY_GOOD));
	}

	@NotNull
	@Override
	public Bot getBot()
	{
		return this.context;
	}

	@Override
	public boolean isIntercepted()
	{
		return false;
	}

	@Override
	public void intercept()
	{
	}

	public Json data()
	{
		return this.data;
	}

	public boolean online()
	{
		return this.online.getValue();
	}

	public boolean good()
	{
		return this.good.getValue();
	}
}
