package org.mve.data;

public class Query
{
	public static final String MSG1 = "Only one auto increment column and it must be defined as a key";
	public static final String MSG2 = "Only one primary key column allowed";
	public static final int TYPE_MYSQL  = 0;
	public static final int TYPE_SQLITE = 1;

	public <T> Object query(SimpleMapper<T> mapper)
	{
		return mapper.query(this);
	}
}
