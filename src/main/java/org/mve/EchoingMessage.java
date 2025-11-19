package org.mve;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.mve.woden.GradleWrapper;
import org.mve.woden.Woden;

import java.util.concurrent.TimeUnit;
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
		if (text.getContent().startsWith("/woden"))
		{
			// Whether sender is owner
			if (event.getSender().getId() != Configuration.OWNER)
				return false;

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
				return true;
			}
			if (command.equals("stop"))
			{
				this.wednesday.close();
				return true;
			}
			if (command.equals("reload"))
			{
				this.wednesday.close();
				Woden.running = true;
				return true;
			}
			if (command.equals("update"))
			{
				this.wednesday.close();
				Woden.after = () ->
				{
					try
					{
						Process proc = Woden.git("pull");
						boolean exit = proc.waitFor(60, TimeUnit.SECONDS);
						if (!exit)
						{
							Wednesday.LOGGER.error(LoggerMessage.LOG_WEDNESDAY_UPDATE_GIT_TIMEOUT);
							return;
						}

						GradleWrapper.gradle("build");
						Woden.running = true;
					}
					catch (Throwable t)
					{
						Wednesday.LOGGER.error(LoggerMessage.LOG_WEDNESDAY_UPDATE_ERROR, t);
					}
				};
			}
		}
		return false;
	}
}
