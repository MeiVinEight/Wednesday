package org.mve.orange.data;

import net.mamoe.mirai.data.StrangerInfo;

public class WrappedStrangerInfo extends WrappedUser implements StrangerInfo
{
	public WrappedStrangerInfo(long ID, int group, String nick, String remark)
	{
		super(ID, group, nick, remark);
	}

	@Override
	public long getFromGroup()
	{
		return this.group;
	}
}
