package org.mve.web;

import org.mve.data.Creation;
import org.mve.data.Database;
import org.mve.data.Query;
import org.mve.data.SimpleMapper;

import javax.persistence.Table;

@Table(Persistence.TABLE_NAME)
public class Persistence
{
	public static final String TABLE_NAME = "PERSISTENCE";
	public static final SimpleMapper<Persistence> MAPPER = new SimpleMapper<>(new Database("jdbc:sqlite:data/PERSISTENCE.DB", null, null), Persistence.class);
	public String NAME;
	public String TEXT;
	public Integer AUTH;

	public Persistence()
	{
		this(null, null);
	}

	public Persistence(String NAME, String TEXT)
	{
		this(NAME, TEXT, true);
	}

	public Persistence(String NAME, String TEXT, boolean auth)
	{
		this.NAME = NAME;
		this.TEXT = TEXT;
		this.AUTH = auth ? 1 : 0;
	}

	public boolean auth()
	{
		return this.AUTH != 0;
	}

	static
	{
		boolean created = new Creation(TABLE_NAME)
			.db(Query.TYPE_SQLITE)
			.column("NAME", "VARCHAR(255)")
			.primary()
			.notnull()
			.unique()
			.column("TEXT", "TEXT")
			.notnull()
			.column("AUTH", "BOOLEAN")
			.notnull()
			.query(Persistence.MAPPER);
		if (created)
			WebAPI.LOGGER.info("创建表 {}", TABLE_NAME);
	}
}
