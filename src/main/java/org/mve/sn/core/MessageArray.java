package org.mve.sn.core;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageSource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

public class MessageArray implements Consumer<MessageEvent>
{
	public static int MAX_QUEUE_SIZE = 4096;
	private final Map<Integer, MessageSource> source = new HashMap<>();
	private final Queue<Integer> queue = new LinkedList<>();

	@Override
	public void accept(MessageEvent event)
	{
		this.push(event.getSource());
	}

	public void push(MessageSource source)
	{
		while (!this.queue.isEmpty() && (this.queue.size() >= MAX_QUEUE_SIZE))
			this.source.remove(queue.poll());
		this.queue.add(source.getIds()[0]);
		this.source.put(source.getIds()[0], source);
	}

	public MessageSource get(int id)
	{
		return this.source.get(id);
	}
}
