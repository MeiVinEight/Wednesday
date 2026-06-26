package org.mve.sn.message;

import net.mamoe.mirai.message.data.MessageContent;
import org.jetbrains.annotations.NotNull;
import org.mve.uni.Json;

public class UnknownMessage implements MessageContent, MessageJson
{
	public final Json message;

	public UnknownMessage(Json message)
	{
		this.message = message;
		SupernovaMessage.LOGGER.getValue().warning("未知消息: " + message);
	}

	@NotNull
	@Override
	public String contentToString()
	{
		return "";
	}

	@Override
	public Json json()
	{
		return this.message;
	}
}
