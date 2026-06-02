package org.mve.sn.data;

import net.mamoe.mirai.data.StrangerInfo;

public class StrangerInfoW extends UserW implements StrangerInfo
{
	public StrangerInfoW(long ID, int group, String nick, String remark)
	{
		super(ID, group, nick, remark);
	}

	@Override
	public long getFromGroup()
	{
		return this.group;
	}
}
