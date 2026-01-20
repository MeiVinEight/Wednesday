package org.mve;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.mve.data.Database;
import org.mve.data.SimpleMapper;
import org.mve.service.ApplicationMessage;
import org.mve.service.NudgeFacing;
import org.mve.vo.Facing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<Event, ListeningStatus>
{
	private final Wednesday wednesday;
	private final Listener<Event> subscribe;
	private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
	private final Map<Class<? extends Event>, Consumer<Event>> event = new HashMap<>();
	private final Map<String, Function<MessageEvent, Boolean>> command = new HashMap<>();
	private final SimpleMapper<Facing> facing;
	private final Random random = new Random();

	public SubscribeMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
		this.subscribe = this.wednesday.QQ.getEventChannel().subscribe(Event.class, this);
		String mysqlUrl = "jdbc:mysql://" + Configuration.MYSQL_HOST + ':' + Configuration.MYSQL_PORT + "/COFFEE";
		this.facing = new SimpleMapper<>(new Database(mysqlUrl, Configuration.MYSQL_USERNAME, Configuration.MYSQL_PASSWORD));
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

	public void register(String cmd, Function<MessageEvent, Boolean> listener)
	{
		this.command.put(cmd, listener);
	}

	@Override
	public void run()
	{
		Event event = queue.poll();
		if (event == null)
			return;
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
			this.command.forEach((pfx, listener) ->
			{
				if (contentWithoutPrefix.startsWith(pfx))
					listener.apply(messageEvent);
			});
		}
		if (event instanceof BotOfflineEvent)
			this.wednesday.close();
		if (event instanceof NudgeEvent nudge)
		{
			if (nudge.getTarget().getId() == nudge.getBot().getId())
			{
				int count = this.facing.count("FACING");
				Wednesday.LOGGER.debug("{} Facings", count);
				Facing fac = new Facing();
				fac.ID = random.nextInt(count);
				fac = this.facing.primary(fac);
				String url = Configuration.FILE_SERVER + "/image/" + fac.NAME;
				Image image = Image.fromId(url);
				if (url.startsWith("file:///"))
				{
					File file = new File(url.substring("file:///".length()));
					if (!file.isFile())
					{
						Wednesday.LOGGER.error("File {} not found", file);
						return;
					}
					image = Contact.uploadImage(nudge.getSubject(), file);
				}
				nudge.getSubject().sendMessage(image);
			}
		}
	}

	public void cancel()
	{
		super.cancel();
		this.subscribe.cancel(new CancellationException("close"));
	}
}
