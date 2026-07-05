package org.mve.orange.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.BotPassiveEvent;
import org.jetbrains.annotations.NotNull;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;

public class PostingMessageEvent implements BotPassiveEvent
{
	public final Orange context;
	public final String text;
	public final Json origin;
	public final long ID;
	public final Json message;
	public final String type;
	public final long time;

	public PostingMessageEvent(Orange context, String json)
	{
		this.context = context;
		this.text = json;
		this.origin = Json.resolve(json);
		this.ID = origin.number(OrangeAPI.KEY_MESSAGE_ID).longValue();
		this.message = this.origin.get(OrangeAPI.KEY_MESSAGE);
		this.type = this.origin.string(OrangeAPI.KEY_MESSAGE_TYPE);
		this.time = this.origin.number(OrangeAPI.KEY_TIME).longValue();
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
