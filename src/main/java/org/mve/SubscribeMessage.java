package org.mve;

import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<Event, ListeningStatus>
{
	private final Wednesday wednesday;
	private final Queue<MessageEvent> queue = new ConcurrentLinkedQueue<>();
	private final LinkedList<Function<MessageEvent, Boolean>> listeners = new LinkedList<>();

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
		for (Function<MessageEvent, Boolean> listener : this.listeners)
			if (listener.apply(event))
				return;
	}
}
