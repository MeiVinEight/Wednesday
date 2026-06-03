package org.mve.sn.core.contact;

import kotlin.Lazy;
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
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class SupernovaFriend extends SupernovaUser implements Friend
{
	private final Lazy<String> nickname;
	private final String remark;

	public SupernovaFriend(Bot context, FriendInfo info)
	{
		super(context, info.getUin());
		LazyJVM<String> lazyJVM = new LazyJVM<>(() -> {
			Json json = SupernovaAPI.getStrangerInfo((Supernova) (this.getBot()), this.getId(), true);
			Json data = json.get(SupernovaAPI.KEY_DATA);
			return data.string(SupernovaAPI.KEY_NICKNAME);
		});
		lazyJVM.setValue(info.getNick());
		this.nickname = lazyJVM;
		this.remark = info.getRemark();
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
		return this.remark;
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

	@NotNull
	@Override
	public String getNick()
	{
		return this.nickname.getValue();
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
