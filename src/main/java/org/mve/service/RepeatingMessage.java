package org.mve.service;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.mve.Wednesday;

import java.util.Random;
import java.util.Set;

public class RepeatingMessage
{
	private static final Random RANDOM = new Random();
	private final double probability;
	private final Set<Class<? extends Message>> filter;

	public RepeatingMessage(double probability, Set<Class<? extends Message>> filter)
	{
		if (probability < 0 || probability > 1)
			throw new IllegalArgumentException("probability must be between 0 and 1");
		this.probability = probability;
		this.filter = filter;
	}

	public void random(GroupMessageEvent messageEvent)
	{
		double randomValue = RANDOM.nextDouble();
		Wednesday.LOGGER.debug("REPEAT PROBABILIRY: {}: {}", randomValue, this.probability);
		if (randomValue > probability)
			return;
		MessageChain chain = messageEvent.getMessage();
		MessageChainBuilder builder = new MessageChainBuilder();
		builder.addAll(chain.stream()
			.filter(s -> !filter.contains(s.getClass()))
			.toList());
		if (builder.isEmpty())
			return;
		messageEvent.getSubject().sendMessage(builder.build());
	}
}
