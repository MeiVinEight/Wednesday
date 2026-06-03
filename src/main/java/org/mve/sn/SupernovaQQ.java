package org.mve.sn;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.sequences.Sequence;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.IMirai;
import net.mamoe.mirai._MiraiInstance;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.OtherClientInfo;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.data.FriendInfo;
import net.mamoe.mirai.data.MemberInfo;
import net.mamoe.mirai.data.StrangerInfo;
import net.mamoe.mirai.data.UserProfile;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.message.action.Nudge;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.MessageSourceKind;
import net.mamoe.mirai.message.data.OfflineMessageSource;
import net.mamoe.mirai.message.data.UnsupportedMessage;
import net.mamoe.mirai.utils.FileCacheStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.sn.core.contact.SupernovaFriend;
import org.mve.sn.core.contact.SupernovaStranger;

import java.util.List;

public class SupernovaQQ implements IMirai
{
	public static final SupernovaQQ SUPERNOVA = new SupernovaQQ();

	@NotNull
	@Override
	public BotFactory getBotFactory()
	{
		return null;
	}

	@NotNull
	@Override
	public FileCacheStrategy getFileCacheStrategy()
	{
		return null;
	}

	@Override
	public void setFileCacheStrategy(@NotNull FileCacheStrategy fileCacheStrategy)
	{

	}

	@Nullable
	@Override
	public Object recallMessage(@NotNull Bot bot, @NotNull MessageSource messageSource, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object sendNudge(@NotNull Bot bot, @NotNull Nudge nudge, @NotNull Contact contact, @NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public FileMessage createFileMessage(@NotNull String s, int i, @NotNull String s1, long l)
	{
		return null;
	}

	@NotNull
	@Override
	public UnsupportedMessage createUnsupportedMessage(@NotNull byte[] bytes)
	{
		return null;
	}

	@Nullable
	@Override
	public Object queryImageUrl(@NotNull Bot bot, @NotNull Image image, @NotNull Continuation<? super String> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object queryProfile(@NotNull Bot bot, long l, @NotNull Continuation<? super UserProfile> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public OfflineMessageSource constructMessageSource(long l, @NotNull MessageSourceKind messageSourceKind, long l1, long l2, @NotNull int[] ints, int i, @NotNull int[] ints1, @NotNull MessageChain messageChain)
	{
		return null;
	}

	@Nullable
	@Override
	public Object downloadLongMessage(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super MessageChain> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object downloadForwardMessage(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super List<ForwardMessage.Node>> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object acceptNewFriendRequest(@NotNull NewFriendRequestEvent newFriendRequestEvent, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object rejectNewFriendRequest(@NotNull NewFriendRequestEvent newFriendRequestEvent, boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object acceptMemberJoinRequest(@NotNull MemberJoinRequestEvent memberJoinRequestEvent, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object rejectMemberJoinRequest(@NotNull MemberJoinRequestEvent memberJoinRequestEvent, boolean b, @NotNull String s, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object getOnlineOtherClientsList(@NotNull Bot bot, boolean b, @NotNull Continuation<? super List<OtherClientInfo>> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object ignoreMemberJoinRequest(@NotNull MemberJoinRequestEvent memberJoinRequestEvent, boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object acceptInvitedJoinGroupRequest(@NotNull BotInvitedJoinGroupRequestEvent botInvitedJoinGroupRequestEvent, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object ignoreInvitedJoinGroupRequest(@NotNull BotInvitedJoinGroupRequestEvent botInvitedJoinGroupRequestEvent, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object broadcastEvent(@NotNull Event event, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object refreshKeys(@NotNull Bot bot, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Friend newFriend(@NotNull Bot bot, @NotNull FriendInfo friendInfo)
	{
		return new SupernovaFriend(bot, friendInfo);
	}

	@NotNull
	@Override
	public Stranger newStranger(@NotNull Bot bot, @NotNull StrangerInfo strangerInfo)
	{
		return new SupernovaStranger(bot, strangerInfo.getUin());
	}

	@Nullable
	@Override
	public Object recallGroupMessageRaw(@NotNull Bot bot, long l, @NotNull int[] ints, @NotNull int[] ints1, @NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object recallFriendMessageRaw(@NotNull Bot bot, long l, @NotNull int[] ints, @NotNull int[] ints1, int i, @NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object recallGroupTempMessageRaw(@NotNull Bot bot, long l, long l1, @NotNull int[] ints, @NotNull int[] ints1, int i, @NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object getRawGroupList(@NotNull Bot bot, @NotNull Continuation<? super Sequence<Long>> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object getRawGroupMemberList(@NotNull Bot bot, long l, long l1, long l2, @NotNull Continuation<? super Sequence<? extends MemberInfo>> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object solveNewFriendRequestEvent(@NotNull Bot bot, long l, long l1, @NotNull String s, boolean b, boolean b1, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object solveBotInvitedJoinGroupRequestEvent(@NotNull Bot bot, long l, long l1, long l2, boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object solveMemberJoinRequestEvent(@NotNull Bot bot, long l, long l1, @NotNull String s, long l2, @Nullable Boolean aBoolean, boolean b, @NotNull String s1, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object getGroupVoiceDownloadUrl(@NotNull Bot bot, @NotNull byte[] bytes, long l, long l1, @NotNull Continuation<? super String> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object muteAnonymousMember(@NotNull Bot bot, @NotNull String s, @NotNull String s1, long l, int i, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	static
	{
		_MiraiInstance.set(SUPERNOVA);
	}
}
