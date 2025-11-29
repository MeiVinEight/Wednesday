package org.mve;

import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<Event, ListeningStatus>
{
	private final Wednesday wednesday;
	private final Queue<MessageEvent> queue = new ConcurrentLinkedQueue<>();
	private final Map<String, Function<MessageEvent, Boolean>> listeners = new HashMap<>();

	public SubscribeMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
	}

	@Override
	public ListeningStatus apply(Event event)
	{
		if (event instanceof MessageEvent me)
			return this.apply(me);
		else if (event instanceof BotOfflineEvent)
			this.wednesday.close();
		return ListeningStatus.LISTENING;
	}

	public ListeningStatus apply(MessageEvent event)
	{
		// Add event to queue and handle event in another thread
		this.queue.add(event);
		return ListeningStatus.LISTENING;
	}

	public void register(String cmd, Function<MessageEvent, Boolean> listener)
	{
		this.listeners.put(cmd, listener);
	}

	@Override
	public void run()
	{
		MessageEvent event = queue.poll();
		if (event == null)
			return;
		MessageChain msg = event.getMessage();
		if (!(msg.get(1) instanceof PlainText text))
			return;
		String content = text.getContent();
		if (!content.startsWith(Configuration.COMMAND_PREFIX))
			return;
		content = content.substring(1);
		String contentWithoutPrefix = content;
		this.listeners.forEach((pfx, listener) ->
		{
			if (contentWithoutPrefix.startsWith(pfx))
				listener.apply(event);
		});
	}
}
