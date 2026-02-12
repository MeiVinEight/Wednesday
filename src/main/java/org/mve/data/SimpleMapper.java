package org.mve.data;

import org.mve.Wednesday;
import org.mve.invoke.ConstructorAccessor;
import org.mve.invoke.FieldAccessor;
import org.mve.invoke.MagicAccessor;
import org.mve.invoke.ReflectionFactory;
import org.mve.invoke.common.JavaVM;
import org.mve.uni.Mirroring;
import org.sqlite.core.CoreResultSet;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleMapper<T> extends Mapper<T>
{
	public final Class<T> clazz;

	public SimpleMapper(Database database, Class<T> clazz)
	{
		super(database);
		this.clazz = clazz;
	}

	@Override
	public boolean insert(T o)
	{
		Class<?> clazz = o.getClass();
		String tableName = o.getClass().getSimpleName();
		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null) tableName = tableAnno.value();

		try (Connection conn = this.connection())
		{
			StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(' ');
			StringBuilder columnBuilder = new StringBuilder("(");
			StringBuilder valuesBuilder = new StringBuilder("(");
			Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(clazz))
				.filter(x -> (x.getModifiers() & Modifier.STATIC) == 0)
				.toArray(Field[]::new);
			List<Object> args = new ArrayList<>();
			for (Field field : fields)
			{
				String columnName = columnName(field);
				if (columnName == null)
					continue;
				FieldAccessor<?> facc = ReflectionFactory.access(field);
				Object val = facc.get(o);
				if (val == null)
					continue;
				if (!args.isEmpty())
				{
					columnBuilder.append(", ");
					valuesBuilder.append(", ");
				}
				columnBuilder.append(columnName);
				valuesBuilder.append('?');
				args.add(val);
			}
			columnBuilder.append(')');
			valuesBuilder.append(')');
			sql.append(columnBuilder).append(" VALUES ").append(valuesBuilder).append(';');
			Wednesday.LOGGER.verbose(sql.toString());

			try (PreparedStatement stmt = conn.prepareStatement(sql.toString()))
			{
				for (int i = 0; i < args.size(); i++)
				{
					stmt.setObject(i + 1, args.get(i));
				}
				int x = stmt.executeUpdate();
				return x > 0;
			}
		}
		catch (SQLException t)
		{
			Wednesday.LOGGER.error(t);
		}
		return false;
	}

	@Override
	public T primary(T o)
	{
		Class<?> clazz = o.getClass();
		String tableName = o.getClass().getSimpleName();
		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null)
		{
			tableName = tableAnno.value();
		}
		T t = null;
		try (Connection conn = this.connection())
		{
			Set<String> primKeys = this.primaryKey(tableName);

			Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(clazz))
				.filter(x -> (x.getModifiers() & Modifier.STATIC) == 0)
				.toArray(Field[]::new);
			List<FieldAccessor<?>> primaryKeys = new LinkedList<>();
			StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");

			for (Field field : fields)
			{
				String columnName = columnName(field);
				if (primKeys.contains(columnName))
				{
					if (!primaryKeys.isEmpty())
					{
						sql.append(" AND ");
					}
					primaryKeys.add(ReflectionFactory.access(field));
					sql.append(columnName)
						.append(" = ?");
				}
			}
			sql.append(';');
			Wednesday.LOGGER.verbose(sql.toString());

			try (PreparedStatement stmt = conn.prepareStatement(sql.toString()))
			{
				// for (FieldAccessor<?> facc : primaryKeys)
				for (int i = 0; i < primaryKeys.size(); i++)
				{
					FieldAccessor<?> facc = primaryKeys.get(i);
					stmt.setObject(i + 1, facc.get(o));
				}

				try (ResultSet rs = stmt.executeQuery())
				{
					if (rs.next())
					{
						Constructor<T> noArg = MagicAccessor.accessor.getConstructor(clazz);
						if (noArg == null) throw new NullPointerException("No argument constructor not found for " + clazz);
						ConstructorAccessor<T> ctor = ReflectionFactory.access(noArg);
						t = ctor.invoke();
						SimpleMapper.convert(rs, t);
					}
				}
			}

		}
		catch (Throwable exce)
		{
			Wednesday.LOGGER.error(exce);
		}
		return t;
	}

	public List<T> select(Class<T> clazz, Object... where)
	{
		Map<String, Object> column = new HashMap<>();
		for (int i = 0; i < where.length; i += 2)
			column.put(where[i].toString(), where[i + 1]);
		return this.select(column, clazz);
	}

	@Override
	public List<T> select(Map<String, Object> where, Class<T> clazz)
	{
		String tableName = clazz.getSimpleName();
		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null) tableName = tableAnno.value();
		List<T> retVal = new LinkedList<>();

		try (Connection conn = this.connection())
		{
			StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
			Object[] args = new Object[where.size()];
			int idx = 0;
			for (String key : where.keySet())
			{
				Object value = where.get(key);
				if (idx > 0) sql.append(" AND ");
				sql.append(key).append(" = ?");
				args[idx++] = value;
			}
			sql.append(';');
			Wednesday.LOGGER.verbose(sql.toString());
			try (PreparedStatement stmt = conn.prepareStatement(sql.toString()))
			{
				for (int i = 0; i < idx; i++)
					stmt.setObject(i + 1, args[i]);

				try (ResultSet rs = stmt.executeQuery())
				{
					while (rs.next())
					{
						Constructor<T> noArg = MagicAccessor.accessor.getConstructor(clazz);
						if (noArg == null) throw new NullPointerException("No argument constructor not found for " + clazz);
						ConstructorAccessor<T> ctor = ReflectionFactory.access(noArg);
						T t = ctor.invoke();
						SimpleMapper.convert(rs, t);
						retVal.add(t);
					}
				}
			}
		}
		catch (SQLException e)
		{
			Wednesday.LOGGER.error(e);
		}
		return retVal;
	}

	@Override
	public boolean update(T o)
	{
		return this.update(o, this.primaryKey(SimpleMapper.table(o.getClass())).toArray(String[]::new)) != 0;
	}

	public int update(T o, String... whereCol)
	{
		Map<String, Object> set = new HashMap<>();
		Map<String, Object> whr = new HashMap<>();
		Set<String> wheres = Set.of(whereCol);
		// Set<String> primKeys = this.primaryKey(tableName);
		Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(clazz))
			.filter(x -> (x.getModifiers() & Modifier.STATIC) == 0)
			.toArray(Field[]::new);
		for (Field field : fields)
		{
			String columnName = columnName(field);
			if (columnName == null) continue;
			FieldAccessor<?> accessor = ReflectionFactory.access(field);
			Object val = accessor.get(o);
			if (val == null) continue;
			if (wheres.contains(columnName))
				whr.put(columnName, val);
			else
				set.put(columnName, val);
		}
		return this.update(set, whr);
	}

	public int update(Map<String, Object> set, Map<String, Object> where)
	{
		String tableName = table(this.clazz);
		try (Connection conn = this.connection())
		{
			StringBuilder queryStmt = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
			StringBuilder whereStmt = new StringBuilder(" WHERE ");

			// Set<String> primKeys = this.primaryKey(tableName);
			Object[] setArg = new Object[set.size()];
			Object[] whrArg = new Object[where.size()];
			int idxs = 0;
			int idxw = 0;
			// for (Field field : fields)
			for (Map.Entry<String, Object> entry : set.entrySet())
			{
				String columnName = entry.getKey();
				if (idxs > 0) queryStmt.append(", ");
				queryStmt.append(columnName).append(" = ?");
				setArg[idxs++] = entry.getValue();
			}
			for (Map.Entry<String, Object> entry : where.entrySet())
			{
				String columnName = entry.getKey();
				if (idxw > 0) whereStmt.append(" AND ");
				whereStmt.append(columnName).append(" = ?");
				whrArg[idxw++] = entry.getValue();
			}

			queryStmt.append(whereStmt).append(';');
			Wednesday.LOGGER.verbose(queryStmt.toString());
			try (PreparedStatement stmt = conn.prepareStatement(queryStmt.toString()))
			{
				for (int i = 0; i < idxs; i++)
					stmt.setObject(i + 1, setArg[i]);
				for (int i = 0; i < idxw; i++)
					stmt.setObject(idxs + i + 1, whrArg[i]);
				return stmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			Mirroring.thrown(e);
		}
		return 0;
	}

	@Override
	public boolean delete(T o)
	{
		Map<String ,Object> col = new HashMap<>();
		Class<?> clazz = o.getClass();
		String tableName = table(clazz);
		Set<String> primKeys = this.primaryKey(tableName);
		Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(clazz))
			.filter(x -> (x.getModifiers() & Modifier.STATIC) == 0)
			.toArray(Field[]::new);
		for (Field field : fields)
		{
			String columnName = columnName(field);
			if (columnName == null)
				continue;
			if (primKeys.contains(columnName))
				col.put(columnName, ReflectionFactory.access(field).get(o));
		}
		return this.delete(tableName, col) > 0;
	}

	public int delete(Class<T> clazz, Object... params)
	{
		String tableName = table(clazz);
		Map<String, Object> column = new HashMap<>();
		for (int i = 0; i < params.length; i += 2)
			column.put(params[i].toString(), params[i + 1]);
		return this.delete(tableName, column);
	}

	public int delete(String tableName, Map<String, Object> column)
	{
		try (Connection conn = this.connection())
		{
			StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE ");
			int idx = 0;
			Object[] args = new Object[column.size()];
			for (Map.Entry<String, Object> entry : column.entrySet())
			{
				if (idx != 0)
					sql.append(" AND ");
				sql.append(entry.getKey()).append(" = ?");
				args[idx++] = entry.getValue();
			}

			sql.append(';');
			Wednesday.LOGGER.verbose(sql.toString());
			try (PreparedStatement stmt = conn.prepareStatement(sql.toString()))
			{
				for (idx = 0; idx < args.length; idx++)
					stmt.setObject(idx + 1, args[idx]);
				return stmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			Wednesday.LOGGER.error(e);
		}
		return 0;
	}

	public int count(String tableName)
	{
		try (Connection conn = this.connection(); PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName + ";"))
		{
			try (ResultSet rs = stmt.executeQuery())
			{
				if (rs.next())
				{
					return rs.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			Wednesday.LOGGER.error(e);
		}
		return 0;
	}

	public int count(T o)
	{
		Class<?> clazz = o.getClass();
		String tableName = o.getClass().getSimpleName();
		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null)
		{
			tableName = tableAnno.value();
		}

		try (Connection conn = this.connection())
		{
			StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ")
				.append(tableName)
				.append(" WHERE ");
			Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(clazz))
				.filter(x -> !Modifier.isStatic(x.getModifiers()))
				.toArray(Field[]::new);
			Object[] args = new Object[fields.length];
			int argCount = 0;
			for (Field field : fields)
			{
				String columnName = columnName(field);
				if (columnName == null)
					continue;
				FieldAccessor<?> facc = ReflectionFactory.access(field);
				Object value = facc.get(o);
				if (value == null) continue;
				if (argCount > 0) sql.append(" AND ");
				sql.append(columnName).append(" = ?");
				args[argCount++] = value;
			}
			sql.append(';');
			Wednesday.LOGGER.verbose(sql.toString());
			try (PreparedStatement stmt = conn.prepareStatement(sql.toString()))
			{
				for (int i = 0; i < argCount; i++)
					stmt.setObject(i + 1, args[i]);
				try (ResultSet rs = stmt.executeQuery())
				{
					if (rs.next())
						return rs.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			Wednesday.LOGGER.error(e);
		}
		return 0;
	}

	public boolean create(Creation c)
	{
		try (Connection conn = this.connection())
		{
			PreparedStatement stmt = conn.prepareStatement(c.toString());
			return stmt.execute();
		}
		catch (SQLException e)
		{
			Mirroring.thrown(e);
		}
		return false;
	}

	public Object query(Query query)
	{
		Object obj1 = null;
		Object[] obj2 = null;
		Object[][] obj3 = null;
		try (Connection conn = this.connection())
		{
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			try (ResultSet rs = stmt.executeQuery())
			{
				CoreResultSet crs = (CoreResultSet) rs;
				int rows = Math.toIntExact(crs.maxRows);
				int cols = crs.colsMeta.length;
				if (rows == 0 && cols == 0)
					return null;
				if (rows == 1 && cols == 1)
				{
					rs.next();
					return rs.getObject(1);
				}
				if (rows == 1)
					obj1 = obj2 = new Object[cols];
				else if (cols == 1)
					obj1 = obj2 = new Object[rows];
				else
					obj1 = obj3 = new Object[rows][cols];
				int i = 0;
				while (rs.next())
				{
					for (int j = 0; j < cols; j++)
					{
						if (rows == 1)
							obj2[j] = rs.getObject(j + 1);
						else if (cols == 1)
							obj2[i] = rs.getObject(j + 1);
						else
							obj3[i][j] = rs.getObject(j + 1);
					}
					i++;
				}
			}
		}
		catch (SQLException e)
		{
			Mirroring.thrown(e);
		}
		return obj1;
	}

	public Set<String> primaryKey(String tableName)
	{
		Set<String> primKeys = new HashSet<>();
		try (Connection conn = this.connection())
		{
			try (ResultSet primKeyRS = conn.getMetaData().getPrimaryKeys(null, null, tableName))
			{
				while (primKeyRS.next())
				{
					primKeys.add(primKeyRS.getString("COLUMN_NAME"));
				}
			}
		}
		catch (SQLException e)
		{
			JavaVM.exception(e);
		}
		return primKeys;
	}

	public static <T> String table(Class<T> clazz)
	{
		String tableName = clazz.getSimpleName();
		Table tableAnno = clazz.getAnnotation(Table.class);
		if (tableAnno != null)
			tableName = tableAnno.value();
		return tableName;
	}

	public static <T> void convert(ResultSet rs, T o) throws SQLException
	{
		Field[] fields = Arrays.stream(MagicAccessor.accessor.getFields(o.getClass()))
			.filter(x -> (x.getModifiers() & Modifier.STATIC) == 0)
			.toArray(Field[]::new);
		for (Field field : fields)
		{
			String columnName = columnName(field);
			if (columnName == null)
				continue;
			FieldAccessor<?> facc = ReflectionFactory.access(field);
			Class<?> columnType = field.getType();
			facc.set(o, rs.getObject(columnName, columnType));
		}
	}

	public static String columnName(Field field)
	{
		String columnName = field.getName();
		Column columnAnno = field.getAnnotation(Column.class);
		if (columnAnno != null)
		{
			if (columnAnno.exclude())
				return null;
			columnName = columnAnno.name();
		}
		return columnName;
	}

	static
	{
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(e);
		}
	}
}
