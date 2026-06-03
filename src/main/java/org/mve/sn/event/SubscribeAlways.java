package org.mve.sn.event;

import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.ListeningStatus;

import java.util.function.Consumer;

public class SubscribeAlways implements Function2<Event, Continuation<? super ListeningStatus>, ListeningStatus>
{
	private final Consumer<Event> consumer;

	public SubscribeAlways(Consumer<Event> consumer, Continuation<? super Function2<Event, Continuation<? super ListeningStatus>, ListeningStatus>> continuation)
	{
		this.consumer = consumer;
	}

	@Override
	public ListeningStatus invoke(Event o, Continuation<? super ListeningStatus> o2)
	{
		this.consumer.accept(o);
		return ListeningStatus.LISTENING;
	}
}
