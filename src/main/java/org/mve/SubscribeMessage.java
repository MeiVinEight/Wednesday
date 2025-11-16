package org.mve;

import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<MessageEvent, ListeningStatus>
{
	private final Queue<MessageEvent> queue = new ConcurrentLinkedQueue<>();
	private final LinkedList<Function<MessageEvent, Boolean>> listeners = new LinkedList<>();

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
