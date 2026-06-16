package org.mve.sn.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.BotPassiveEvent;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;

public class PostingEvent implements BotPassiveEvent
{
	public final Supernova context;
	public final String text;
	public final Json message;
	public final String type;

	public PostingEvent(Supernova context, String message)
	{
		this.context = context;
		this.text = message;
		this.message = Json.resolve(message);
		this.type = this.message.string(SupernovaAPI.KEY_POST_TYPE);
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
}
