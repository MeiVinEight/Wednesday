package org.mve.orange.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.events.BotPassiveEvent;
import org.jetbrains.annotations.NotNull;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;

public class PostingEvent extends AbstractEvent implements BotPassiveEvent
{
	public final Orange context;
	public final String text;
	public final Json message;
	public final String type;

	public PostingEvent(Orange context, String message)
	{
		this.context = context;
		this.text = message;
		this.message = Json.resolve(message);
		this.type = this.message.string(OrangeAPI.KEY_POST_TYPE);
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
