package org.mve.orange.data;

import net.mamoe.mirai.data.FriendInfo;

public class WrappedFriendInfo extends WrappedUser implements FriendInfo
{
	public WrappedFriendInfo(long ID, int group, String nick, String remark)
	{
		super(ID, group, nick, remark);
	}

	@Override
	public int getFriendGroupId()
	{
		return this.group;
	}
}
