package org.mve;

import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.mve.service.ApplicationMessage;
import org.mve.service.NudgeFacing;
import org.mve.uni.Mirroring;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<Event, ListeningStatus>
{
	private final Listener<Event> subscribe;
	private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
	private final Map<Class<? extends Event>, Consumer<Event>> consumation = new HashMap<>();
	private final Map<String, Consumer<MessageEvent>> message = new HashMap<>();
	private final Map<Class<? extends SingleMessage>, BiConsumer<MessageEvent, SingleMessage>> segmentation = new HashMap<>();

	public SubscribeMessage(Wednesday wednesday)
	{
		this.subscribe = wednesday.QQ.getEventChannel().subscribe(Event.class, this);
	}

	@Override
	public ListeningStatus apply(Event event)
	{
		Wednesday.LOGGER.debug(event.toString());
		return this.push(event);
	}

	public ListeningStatus push(Event event)
	{
		// Add event to queue and handle event in another thread
		this.queue.add(event);
		return ListeningStatus.LISTENING;
	}

	public <T extends Event> void register(Class<T> type, Consumer<? super T> consumer)
	{
		this.consumation.put(type, Mirroring.checkcast(consumer));
	}

	public void register(String cmd, Consumer<MessageEvent> listener)
	{
		this.message.put(cmd, listener);
	}

	public <T extends SingleMessage> void register(Class<T> type, BiConsumer<? super MessageEvent, ? super T> consumer)
	{
		this.segmentation.put(type, Mirroring.checkcast(consumer));
	}

	@Override
	public void run()
	{
		Event event = queue.poll();
		if (event == null)
			return;

		this.consumation.forEach((k, v) ->
		{
			if (k.isInstance(event))
				v.accept(event);
		});


		if (event instanceof MessageEvent messageEvent)
		{
			NudgeFacing.capture(messageEvent);
			ApplicationMessage.application(messageEvent);

			MessageChain chain = messageEvent.getMessage();
			if (!(chain.get(1) instanceof PlainText text))
				return;
			String content = text.getContent();
			if (!content.startsWith(Configuration.COMMAND_PREFIX))
				return;
			content = content.substring(1);
			String contentWithoutPrefix = content;
			this.message.forEach((pfx, listener) ->
			{
				if (contentWithoutPrefix.startsWith(pfx))
					listener.accept(messageEvent);
			});


			for (SingleMessage singleMessage : chain)
			{
				this.segmentation.forEach((k, v) ->
				{
					if (k.isInstance(singleMessage))
						v.accept(messageEvent, singleMessage);
				});
			}
		}
	}

	public void cancel()
	{
		super.cancel();
		this.subscribe.cancel(new CancellationException("close"));
	}
}
