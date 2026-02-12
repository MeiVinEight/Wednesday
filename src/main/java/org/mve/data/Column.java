package org.mve.data;

public class Column
{
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
			.append(this.type)
			.append(' ');
		if (this.increment)
			builder.append("AUTO_INCREMENT");
		else
		{
			if (this.notnull)
				builder.append("NOT ");
			builder.append("NULL");
		}
		if (this.primary)
			builder.append(" PRIMARY KEY");
		if (this.comment != null)
			builder.append(" COMMENT '")
				.append(this.comment)
				.append("'");
		return builder.toString();
	}
}
