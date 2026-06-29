package org.mve;

import net.mamoe.mirai.contact.AvatarSpec;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import org.mve.uni.Json;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Table(ConnectionWednesday.TABLE_NAME)
public class ConnectionWednesday
{
	public static final String TABLE_NAME = "CONNECTION";
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_TOKEN = "token";
	public Integer ID;
	public String NAME;
	public String URL;
	public String TOKEN;
	@Column(exclude = true)
	public Wednesday connection;
	@Column(exclude = true)
	private final ReentrantLock lock = new ReentrantLock();

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

	public Wednesday connect(boolean reconnect)
	{
		/*
		lock.lock();
		try
		{
			if (this.connection == null)
			{
				this.connection = new Wednesday(this.URL, this.TOKEN);
				this.connection.QQ.getEventChannel().subscribeAlways(BotOfflineEvent.class, (event) -> this.connect(true));
			}
			else if (reconnect)
			{
				this.connection.close();
				this.connection = new Wednesday(this.URL, this.TOKEN);
				this.connection.QQ.getEventChannel().subscribeAlways(BotOfflineEvent.class, (event) -> this.connect(true));
			}
		}
		catch (Throwable t)
		{
			if (this.connection != null)
				this.connection.close();
			this.connection = null;
			Wednesday.LOGGER.error("连接失败: ", t);
		}
		lock.unlock();
		*/
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

	public Json data()
	{
		Json json = new Json();
		/*
		json.set(KEY_NAME, this.NAME);
		json.set(KEY_URL, this.URL);
		json.set(KEY_TOKEN, this.TOKEN);
		Wednesday conn = this.connection;
		if (conn != null)
		{
			Json info = new Json();
			info.set("id", this.connection.QQ.getId());
			info.set("name", this.connection.QQ.getNick());
			info.set("avatar", this.connection.QQ.getAvatarUrl(AvatarSpec.ORIGINAL));
			Json friends = new Json(Json.TYPE_ARRAY);
			conn.QQ.getFriends().forEach(f ->
			{
				Json friend = new Json();
				friend.set("id", f.getId());
				friend.set("name", f.getNick());
				friend.set("avatar", f.getAvatarUrl(AvatarSpec.ORIGINAL));
				friends.add(friend);
			});
			info.set("friends", friends);
			Json groups = new Json(Json.TYPE_ARRAY);
			conn.QQ.getGroups().forEach(g ->
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

		*/
		return json;
	}

	public static ConnectionWednesday resolve(Json data)
	{
		String name = data.string(KEY_NAME);
		String url = data.string(KEY_URL);
		String token = data.string(KEY_TOKEN);
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
