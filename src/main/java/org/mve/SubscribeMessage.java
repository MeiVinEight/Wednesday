package org.mve;

import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<MessageEvent, ListeningStatus>
{
	private final Wednesday wednesday;
	private final Queue<MessageEvent> queue = new ConcurrentLinkedQueue<>();
	private final LinkedList<Function<MessageEvent, Boolean>> listeners = new LinkedList<>();

	public SubscribeMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
	}

	public ListeningStatus apply(MessageEvent event)
	{
		// Add event to queue and handle event in another thread
		this.queue.add(event);
		return ListeningStatus.LISTENING;
	}

	public void register(Function<MessageEvent, Boolean> listener)
	{
		this.listeners.add(listener);
	}

	@Override
	public void run()
	{
		MessageEvent event = queue.poll();
		if (event == null)
			return;
		MessageChain msg = event.getMessage();
		if (msg.size() <= 1)
			return;
		if (!(msg.get(1) instanceof PlainText text))
			return;
		if (text.getContent().startsWith("/echo"))
		{
			MessageChainBuilder builder = new MessageChainBuilder();
			builder.append(new PlainText(text.getContent().substring(5).trim()));
			builder.addAll(msg.stream().skip(2).toList());
			event.getSubject().sendMessage(builder.build());
			return;
		}
		if (text.getContent().startsWith("/stop"))
		{
			this.wednesday.close();
			return;
		}
		for (Function<MessageEvent, Boolean> listener : this.listeners)
			if (listener.apply(event))
				return;
	}
}
