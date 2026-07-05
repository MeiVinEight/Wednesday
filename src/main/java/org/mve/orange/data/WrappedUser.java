package org.mve.orange.data;

import org.jetbrains.annotations.NotNull;

public class WrappedUser
{
	public long ID;
	public int group;
	public String nick;
	public String remark;

	public WrappedUser(long ID, int group, String nick, String remark)
	{
		this.ID = ID;
		this.group = group;
		this.nick = nick;
		this.remark = remark;
	}

	public long getUin()
	{
		return this.ID;
	}

	@NotNull
	public String getNick()
	{
		return this.nick;
	}

	@NotNull
	public String getRemark()
	{
		return this.remark;
	}

	public void setRemark(@NotNull String s)
	{
		this.remark = s;
	}
}
