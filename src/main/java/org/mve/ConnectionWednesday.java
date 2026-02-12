package org.mve;

import net.mamoe.mirai.contact.AvatarSpec;
import org.mve.uni.Json;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;

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

	public Wednesday connect()
	{
		if (this.connection != null)
			return this.connection;
		return this.connection = new Wednesday(this.URL, this.TOKEN);
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
		json.set(KEY_NAME, this.NAME);
		json.set(KEY_URL, this.URL);
		json.set(KEY_TOKEN, this.TOKEN);
		return json;
	}

	public Json info()
	{
		if (this.connection == null)
			return null;
		Json info = this.data();
		Json conn = new Json();
		conn.set("id", this.connection.QQ.getId());
		conn.set("name", this.connection.QQ.getNick());
		conn.set("avatar", this.connection.QQ.getAvatarUrl(AvatarSpec.ORIGINAL));
		info.set("conn", conn);
		return info;
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
