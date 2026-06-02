package org.mve.sn.data;

import net.mamoe.mirai.data.FriendInfo;

public class FriendInfoW extends UserW implements FriendInfo
{
	public FriendInfoW(long ID, int group, String nick, String remark)
	{
		super(ID, group, nick, remark);
	}

	@Override
	public int getFriendGroupId()
	{
		return this.group;
	}
}
