package org.mve;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.util.function.Function;

public class EchoingMessage implements Function<MessageEvent, Boolean>
{
	private final Wednesday wednesday;

	public EchoingMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
	}

	@Override
	public Boolean apply(MessageEvent event)
	{
		MessageChain msg = event.getMessage();
		if (msg.size() <= 1)
			return false;
		if (!(msg.get(1) instanceof PlainText text))
			return false;
		if (text.getContent().startsWith("/echo"))
		{
			MessageChainBuilder builder = new MessageChainBuilder();
			builder.append(text.getContent().substring(5));
			builder.addAll(msg.stream().skip(2).toList());
			MessageChain chain = builder.build();
			if (chain.contentToString().isEmpty())
				event.getSubject().sendMessage(" ");
			else
				event.getSubject().sendMessage(chain);
			return true;
		}
		if (text.getContent().startsWith("/stop"))
		{
			this.wednesday.close();
			return true;
		}
		return false;
	}
}
