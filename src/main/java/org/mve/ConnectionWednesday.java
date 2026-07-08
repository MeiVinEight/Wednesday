package org.mve;

import net.mamoe.mirai.contact.AvatarSpec;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import org.mve.logging.LoggerManager;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.slf4j.Logger;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Table(ConnectionWednesday.TABLE_NAME)
public class ConnectionWednesday
{
	public static final String TABLE_NAME = "CONNECTION";
	public Integer ID;
	public String NAME;
	public String URL;
	public String TOKEN;
	@Column(exclude = true)
	public Orange connection;
	@Column(exclude = true)
	private final ReentrantLock lock = new ReentrantLock();
	@Column(exclude = true)
	private Throwable error;

	public ConnectionWednesday()
	{
		this(null, null, null);
	}

	public ConnectionWednesday(String name, String url, String token)
	{
		this(null, name, url, token);
	}

	public ConnectionWednesday(Integer id, String name, String url, String token)
	{
		this.ID = id;
		this.NAME = name;
		this.URL = url;
		this.TOKEN = (token != null) ? token : "";
	}

	public Orange connect(boolean reconnect)
	{
		Orange conn = this.connection;
		if (conn != null)
			return conn;
		this.lock.lock();
		try
		{
			BotConfiguration configuration = new BotConfiguration();
			configuration.setBotLoggerSupplier(b -> LoggerManager.create(String.valueOf(b.getId())));
			Logger logger = LoggerManager.create("WS");
			if (this.connection == null)
			{
				this.connection = new Orange(this.url(), this.TOKEN, configuration, logger);
			}
			else if (reconnect)
			{
				this.connection.close();
				this.connection = new Orange(this.url(), this.TOKEN, configuration, logger);
			}
			this.connection.getEventChannel().subscribeAlways(BotOfflineEvent.class, e -> this.close());
		}
		catch (Throwable t)
		{
			if (this.connection != null)
				this.connection.close();
			this.connection = null;
			this.error = t;
			Wednesday.LOGGER.error("连接失败: ", t);
		}
		this.lock.unlock();
		return this.connection;
	}

	public void close()
	{
		if (this.connection != null)
		{
			try
			{
				this.connection.close();
			}
			catch (Throwable t)
			{
				Wednesday.LOGGER.error(t);
			}
			this.connection = null;
		}
	}

	public Json data(boolean infos)
	{
		Json json = new Json();
		json.set(WebAPI.KEY_NAME, this.NAME);
		json.set(WebAPI.KEY_URL, this.URL);
		json.set(WebAPI.KEY_TOKEN, this.TOKEN);
		json.set(WebAPI.KEY_ENABLE, this.connection != null);
		if (!infos)
			return json;
		Orange conn = this.connection;
		if (conn != null)
		{
			Json info = new Json();
			info.set("id", conn.getId());
			info.set("name", conn.getNick());
			info.set("avatar", conn.getAvatarUrl(AvatarSpec.ORIGINAL));
			Json friends = new Json(Json.TYPE_ARRAY);
			conn.getFriends().forEach(f ->
			{
				Json friend = new Json();
				friend.set("id", f.getId());
				friend.set("name", f.getNick());
				friend.set("avatar", f.getAvatarUrl(AvatarSpec.ORIGINAL));
				friends.add(friend);
			});
			info.set("friends", friends);
			Json groups = new Json(Json.TYPE_ARRAY);
			conn.getGroups().forEach(g ->
			{
				Json group = new Json();
				group.set("id", g.getId());
				group.set("name", g.getName());
				group.set("avatar", g.getAvatarUrl(AvatarSpec.ORIGINAL));
				groups.add(group);
			});
			info.set("groups", groups);
			json.set("info", info);
		}

		return json;
	}

	public String url()
	{
		return this.URL;
	}

	public Throwable error()
	{
		Throwable e = this.error;
		this.error = null;
		return e;
	}

	public static ConnectionWednesday resolve(Json data)
	{
		String name = data.string(WebAPI.KEY_NAME);
		String url = data.string(WebAPI.KEY_URL);
		String token = data.string(WebAPI.KEY_TOKEN);
		return new ConnectionWednesday(null, name, url, token);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this) return true;
		if (!(o instanceof ConnectionWednesday that)) return false;
		return Objects.equals(NAME, that.NAME) && Objects.equals(URL, that.URL) && Objects.equals(TOKEN, that.TOKEN);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(NAME, URL, TOKEN);
	}
}
