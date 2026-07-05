package org.mve.orange.core.contact;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.friendgroup.FriendGroup;
import net.mamoe.mirai.contact.roaming.RoamingMessages;
import net.mamoe.mirai.data.FriendInfo;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FriendX extends UserX implements Friend
{
	public FriendX(Bot context, FriendInfo info)
	{
		super(context, info.getUin());
		this.nickname.setValue(info.getNick());
		this.remark.setValue(info.getRemark());
	}

	@NotNull
	@Override
	public FriendGroup getFriendGroup()
	{
		return null;
	}

	@Override
	public void setRemark(@NotNull String s)
	{
	}

	@Nullable
	@Override
	public Object delete(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public RoamingMessages getRoamingMessages()
	{
		return null;
	}

	@SuppressWarnings({"rawtypes"})
	@Nullable
	@Override
	public MessageReceipt<Friend> sendMessage(@NotNull Message message, @NotNull Continuation continuation)
	{
		System.out.println("sendMessage: " + message);
		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof FriendX that)) return false;
		return this.getId() == that.getId();
	}

	@Override
	public int hashCode()
	{
		return (int) this.getId();
	}
}
