package org.mve;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.mve.vo.Facing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SubscribeMessage extends Synchronize implements Function<Event, ListeningStatus>
{
	private final Wednesday wednesday;
	private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
	private final Map<String, Function<MessageEvent, Boolean>> listeners = new HashMap<>();
	private final SimpleMapper<Facing> facing;
	private final Random random = new Random();

	public SubscribeMessage(Wednesday wednesday)
	{
		this.wednesday = wednesday;
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
		this.listeners.put(cmd, listener);
	}

	@Override
	public void run()
	{
		Event event = queue.poll();
		if (event == null)
			return;
		if (event instanceof MessageEvent messageEvent)
		{
			MessageChain chain = messageEvent.getMessage();
			for (SingleMessage msg : chain)
			{
				if (msg instanceof WrappedImage wimg)
				{
					if (wimg.getJson() == null)
						continue;
					Json data = Json.resolve(wimg.getJson());
					Wednesday.LOGGER.verbose(data.stringify());
					String summary = data.string("summary");
					if (summary == null || summary.isEmpty())
						continue;

					String fileName = data.string("file");
					Facing fac = new Facing();
					fac.NAME = fileName;
					if (this.facing.count(fac) > 0)
						continue;

					String url = data.string("url");
					try
					{
						URL uploadUrl = new URL("http://meivi.net:83/upload");
						HttpURLConnection conn = (HttpURLConnection) uploadUrl.openConnection();
						conn.setRequestProperty("Download-From", url);
						conn.setRequestProperty("Download-To", "image/" + fileName);

						int respCode = conn.getResponseCode();
						if (respCode != HttpURLConnection.HTTP_OK)
						{
							Wednesday.LOGGER.error("http://meivi.net:83/upload: {}", conn.getResponseMessage());
							continue;
						}

						Json body;
						try (InputStream in = conn.getInputStream())
						{
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							in.transferTo(out);
							body = Json.resolve(out.toString());
						}

						respCode = body.number("code").intValue();
						String message = body.string("message");
						if (respCode != 0 && respCode != 5)
						{
							Wednesday.LOGGER.warn("{}: {}", respCode, message);
							continue;
						}

						data = body.get("data");
						fac.SHA1 = data.string("SHA1");
						this.facing.insert(fac);

					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}


			if (!(chain.get(1) instanceof PlainText text))
				return;
			String content = text.getContent();
			if (!content.startsWith(Configuration.COMMAND_PREFIX))
				return;
			content = content.substring(1);
			String contentWithoutPrefix = content;
			this.listeners.forEach((pfx, listener) ->
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
						return;
					image = Contact.uploadImage(nudge.getSubject(), file);
				}
				nudge.getSubject().sendMessage(image);
			}
		}
	}
}
