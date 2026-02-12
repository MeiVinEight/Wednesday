package org.mve.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Creation extends Query
{
	public int db;
	public String name;
	public LinkedList<Column> columns = new LinkedList<>();
	public Integer increment = null;
	public Column current = null;
	public List<String> primary = new ArrayList<>();
	public List<List<String>> unique = new ArrayList<>();

	public Creation(String name)
	{
		this.name = name;
	}

	public Creation db(int t)
	{
		this.db = t;
		return this;
	}

	public Creation column(String name, String type)
	{
		Column column = new Column();
		column.db = this.db;
		column.name = name;
		column.type = type;
		columns.add(column);
		this.current = column;
		return this;
	}

	public Creation primary(String name, String... columns)
	{
		if (!this.primary.isEmpty())
			throw new IllegalStateException(Query.MSG2);
		this.primary.add(name);
		this.primary.addAll(List.of(columns));
		return this;
	}

	public Creation unique(String name, String... columns)
	{
		List<String> list = new ArrayList<>();
		list.add(name);
		list.addAll(List.of(columns));
		this.unique.add(list);
		return this;
	}

	public Creation notnull()
	{
		this.current.notnull = true;
		return this;
	}

	public Creation increment(int inc)
	{
		if (this.increment != null)
			throw new IllegalStateException(Query.MSG1);
		this.current.increment = true;
		this.current.notnull = true;
		this.increment = inc;
		return this;
	}

	public Creation increment()
	{
		return this.increment(1);
	}

	public Creation primary()
	{
		if (!this.primary.isEmpty())
			throw new IllegalStateException(Query.MSG2);
		this.primary.add(this.current.name);
		this.current.notnull = true;
		this.current.primary = true;
		return this;
	}

	public Creation unique()
	{
		return this.unique(this.current.name, this.current.name);
	}

	@Override
	public <T> Boolean query(SimpleMapper<T> mapper)
	{
		return mapper.create(this);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `")
			.append(this.name)
			.append("` (");
		for (Column col : this.columns)
			builder.append("\n    ")
				.append(col.toString())
				.append(',');
		if (this.primary != null && this.primary.size() > 2)
		{
			builder.append("\n    CONSTRAINT `")
				.append(this.primary.get(0))
				.append("` PRIMARY KEY (");
			for (int i = 1; i <= this.primary.size(); i++)
			{
				if (i > 1)
					builder.append(", ");
				builder.append('`')
					.append(this.primary.get(i))
					.append('`');
			}
			builder.append("),");
		}
		for (List<String> uniqueKey : this.unique)
		{
			builder.append("\n    CONSTRAINT `")
				.append(uniqueKey.get(0))
				.append("` UNIQUE (");
			for (int i = 1; i < uniqueKey.size(); i++)
			{
				if (i > 1)
					builder.append(", ");
				builder.append('`')
					.append(uniqueKey.get(i))
					.append('`');
			}
			builder.append("),");
		}
		builder.setCharAt(builder.length() - 1, '\n');
		builder.append(");");
		return builder.toString();
	}
}
