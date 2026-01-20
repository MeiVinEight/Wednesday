package org.mve.service;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.mve.Configuration;
import org.mve.Wednesday;

import java.util.function.Consumer;

public class EchoingMessage implements Consumer<MessageEvent>
{
	private final Wednesday wednesday;

	public EchoingMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
	}

	@Override
	public void accept(MessageEvent event)
	{
		// Whether sender is owner
		if (event.getSender().getId() != Configuration.OWNER)
			return;
		MessageChain msg = event.getMessage();
		if (msg.size() <= 1)
			return;
		if (!(msg.get(1) instanceof PlainText text))
			return;
		String command = text.getContent().substring(6).stripLeading();
		if (command.startsWith("echo"))
		{
			MessageChainBuilder builder = new MessageChainBuilder();
			builder.append(command.substring(4).stripLeading());
			builder.addAll(msg.stream().skip(2).toList());
			MessageChain chain = builder.build();
			if (chain.contentToString().isEmpty())
				event.getSubject().sendMessage(" ");
			else
				event.getSubject().sendMessage(chain);
			return;
		}
		if (command.equals("stop"))
			this.wednesday.close();
	}
}
