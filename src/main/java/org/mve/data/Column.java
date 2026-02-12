package org.mve.data;

public class Column
{
	public int db;
	public String name;
	public String type;
	public boolean notnull = false;
	public boolean increment = false;
	public boolean primary = false;
	public String comment;

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("`")
			.append(name)
			.append("` ")
			.append(this.type);
		if (this.notnull)
			builder.append(" NOT NULL");
		if (this.primary)
			builder.append(" PRIMARY KEY");
		if (this.increment)
		{
			switch (this.db)
			{
				case Query.TYPE_MYSQL ->  builder.append(" AUTO_INCREMENT");
				case Query.TYPE_SQLITE -> builder.append(" AUTOINCREMENT");
			}
		}
		if (this.comment != null)
			builder.append(" COMMENT '")
				.append(this.comment)
				.append("'");
		return builder.toString();
	}
}
