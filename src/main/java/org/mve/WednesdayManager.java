package org.mve;

import org.mve.data.Creation;
import org.mve.data.Database;
import org.mve.data.Query;
import org.mve.data.SimpleMapper;
import org.mve.logging.LoggerManager;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WednesdayManager
{
	private static final Logger LOGGER = LoggerManager.create("Manager");
	public final Map<String, ConnectionWednesday> connection = new ConcurrentHashMap<>();
	private final SimpleMapper<ConnectionWednesday> data;

	public WednesdayManager(String path)
	{
		path = "jdbc:sqlite:" + path;
		this.data = new SimpleMapper<>(new Database(path, null, null), ConnectionWednesday.class);
		boolean created = new Creation("CONNECTION")
			.db(Query.TYPE_SQLITE)
			.column("ID", "INTEGER")
			.increment()
			.primary()
			.column("NAME", "VARCHAR(64)")
			.notnull()
			.unique()
			.column("URL", "VARCHAR(64)")
			.notnull()
			.column("TOKEN", "VARCHAR(64)")
			.query(this.data);
		if (created)
			LOGGER.info("创建连接表 CONNECTION");
	}

	public void set(ConnectionWednesday connection)
	{
		if (connection.NAME == null)
			return;

		String name = connection.NAME;
		ConnectionWednesday conn;
		boolean equals = false;

		if (((conn = this.connection.get(name)) != null) && !(equals = conn.equals(connection)))
			conn.close();

		if (equals)
			return;

		int count = this.data.update(connection, "NAME");
		if (count == 0)
			this.data.insert(connection);
		conn = this.data.select(ConnectionWednesday.class, "NAME", name).get(0);
		this.connection.put(name, conn);
	}

	public ConnectionWednesday get(String name)
	{
		if (name == null)
			return null;
		ConnectionWednesday conn = this.connection.get(name);
		if (conn != null)
			return conn;
		List<ConnectionWednesday> conns = this.data.select(ConnectionWednesday.class, "NAME", name);
		if (conns == null || conns.isEmpty())
			return null;
		conn = conns.get(0);
		this.connection.put(name, conn);
		return conn;
	}

	public ConnectionWednesday remove(String name)
	{
		ConnectionWednesday conn = this.connection.remove(name);
		if (conn != null)
			conn.close();
		List<ConnectionWednesday> list = this.data.select(ConnectionWednesday.class, "NAME", name);
		if (list != null && !list.isEmpty())
			conn = list.get(0);
		this.data.delete(ConnectionWednesday.class, "NAME", name);
		return conn;
	}

	public ConnectionWednesday[] all()
	{
		ConnectionWednesday[] conns = this.data.select(Map.of())
			.toArray(ConnectionWednesday[]::new);
		for (ConnectionWednesday conn : conns)
		{
			ConnectionWednesday cache = this.connection.get(conn.NAME);
			if (cache != null)
				conn.connection = cache.connection;
		}
		return conns;
	}

	public void close()
	{
		this.connection.forEach((n, c) -> c.close());
	}
}
