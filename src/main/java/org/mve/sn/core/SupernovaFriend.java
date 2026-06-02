package org.mve.sn.core;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.friendgroup.FriendGroup;
import net.mamoe.mirai.contact.roaming.RoamingMessages;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupernovaFriend extends SupernovaUser implements Friend
{
	public SupernovaFriend(Bot context, long id)
	{
		super(context, id);
	}

	@NotNull
	@Override
	public FriendGroup getFriendGroup()
	{
		return null;
	}

	@NotNull
	@Override
	public String getRemark()
	{
		return "";
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

	@Override
	public long getId()
	{
		return 0;
	}

	@NotNull
	@Override
	public RoamingMessages getRoamingMessages()
	{
		return null;
	}

	@NotNull
	@Override
	public String getNick()
	{
		return "";
	}

	@SuppressWarnings({"rawtypes"})
	@Nullable
	@Override
	public MessageReceipt<Friend> sendMessage(@NotNull Message message, @NotNull Continuation continuation)
	{
		System.out.println("sendMessage: " + message);
		return null;
	}
}
