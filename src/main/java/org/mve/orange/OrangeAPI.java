package org.mve.orange;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.sequences.Sequence;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.IMirai;
import net.mamoe.mirai._MiraiInstance;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.MemberPermission;
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
import org.mve.orange.core.APIResponse;
import org.mve.orange.core.Orange;
import org.mve.orange.core.contact.FriendX;
import org.mve.orange.core.contact.StrangerX;
import org.mve.orange.data.SupernovaMessageSource;
import org.mve.orange.event.OrangeManager;
import org.mve.uni.Json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrangeAPI implements IMirai
{
	public static final OrangeAPI API = new OrangeAPI();

	public static final String KEY_ACTION = "action";
	public static final String KEY_ECHO = "echo";
	public static final String KEY_STATUS = "status";
	public static final String KEY_RETCODE = "retcode";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_POST_TYPE = "post_type";
	public static final String KEY_META_EVENT_TYPE = "meta_event_type";
	public static final String KEY_SUBTYPE = "sub_type";
	public static final String KEY_SELF_ID = "self_id";
	public static final String KEY_DATA = "data";
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_APP_VERSION = "app_version";
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_NICKNAME = "nickname";
	public static final String KEY_REMARK = "remark";
	public static final String KEY_PARAMS = "params";
	public static final String KEY_NO_CACHE = "no_cache";
	public static final String KEY_MESSAGE_FORMAT = "message_format";
	public static final String KEY_MESSAGE_TYPE = "message_type";
	public static final String KEY_TIME = "time";
	public static final String KEY_MESSAGE_ID = "message_id";
	public static final String KEY_SENDER = "sender";
	public static final String KEY_RAW_MESSAGE = "raw_message";
	public static final String KEY_TARGET_ID = "target_id";
	public static final String KEY_GROUP_ID = "group_id";
	public static final String KEY_CARD = "card";
	public static final String KEY_ROLE = "role";
	public static final String KEY_AUTO_ESCAPE = "auto_escape";
	public static final String KEY_ONLINE = "online";
	public static final String KEY_GOOD = "good";
	public static final String KEY_GROUP_NAME = "group_name";
	public static final String KEY_SHUT_UP_TIMESTAMP = "shut_up_timestamp";

	public static final String STATUS_OK = "ok";
	public static final String STATUS_FAILED = "failed";
	public static final String STATUS_ASYNC = "async";

	public static final String POST_TYPE_META_EVENT = "meta_event";
	public static final String POST_TYPE_MESSAGE = "message";

	public static final String META_EVENT_LIFECYCLE = "lifecycle";
	public static final String META_EVENT_HEARTBEAT = "heartbeat";
	public static final String LIFECYCLE_CONNECT = "connect";

	public static final String MESSAGE_FORMAT_ARRAY = "array";

	public static final String MESSAGE_TYPE_PRIVATE = "private";
	public static final String MESSAGE_TYPE_GROUP = "group";

	public static final String API_SEND_GROUP_MSG = "send_group_msg";
	public static final String API_GET_VERSION_INFO = "get_version_info";
	public static final String API_GET_FRIEND_LIST = "get_friend_list";
	public static final String API_GET_STRANGER_INFO = "get_stranger_info";
	public static final String API_GET_LOGIN_INFO = "get_login_info";
	public static final String API_GET_GROUP_MEMBER_INFO = "get_group_member_info";
	public static final String API_GET_GROUP_INFO = "get_group_info";

	public static final String ROLE_OWNER = "owner";
	public static final String ROLE_MEMBER = "member";
	public static final String ROLE_ADMIN = "admin";

	public static final Map<Long, Orange> BOT = new ConcurrentHashMap<>();

	public static String getStatus(Orange ws)
	{
		return "";
	}

	public static APIResponse getVersionInfo(Orange sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_VERSION_INFO);
		return sn.communicate(json, false);
	}

	public static APIResponse getFriendList(Orange sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_FRIEND_LIST);
		return sn.communicate(json, false);
	}

	public static APIResponse getStrangerInfo(Orange sn, long userId, boolean noCache)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_STRANGER_INFO)
			.set(KEY_PARAMS, new Json()
				.set(KEY_USER_ID, userId)
				.set(KEY_NO_CACHE, noCache)
			);
		return sn.communicate(json, false);
	}

	public static APIResponse getLoginInfo(Orange sn)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_LOGIN_INFO);
		return sn.communicate(json, false);
	}

	public static APIResponse getGroupMemberInfo(Orange sn, long groupId, long memberId, boolean noCache)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_GROUP_MEMBER_INFO)
			.set(KEY_PARAMS, new Json()
				.set(KEY_GROUP_ID, groupId)
				.set(KEY_USER_ID, memberId)
				.set(KEY_NO_CACHE, noCache)
			);
		return sn.communicate(json, false);
	}

	public static APIResponse sendGroupMessage(Orange sn, long groupId, Json message, boolean rawText)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_SEND_GROUP_MSG)
			.set(KEY_PARAMS, new Json()
				.set(KEY_GROUP_ID, groupId)
				.set(KEY_MESSAGE, message)
				.set(KEY_AUTO_ESCAPE, rawText)
			);
		return sn.communicate(json, false);
	}

	public static APIResponse getGroupInfo(Orange sn, long groupId, boolean noCache)
	{
		Json json = new Json()
			.set(KEY_ACTION, API_GET_GROUP_INFO)
			.set(KEY_PARAMS, new Json()
				.set(KEY_GROUP_ID, groupId)
				.set(KEY_NO_CACHE, noCache)
			);
		return sn.communicate(json, false);
	}

	public static MemberPermission permission(String role)
	{
		if (ROLE_MEMBER.equals(role))
			return MemberPermission.MEMBER;
		if (ROLE_ADMIN.equals(role))
			return MemberPermission.ADMINISTRATOR;
		if (ROLE_OWNER.equals(role))
			return MemberPermission.OWNER;
		return null;
	}

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
		return new SupernovaMessageSource(BOT.get(l), messageSourceKind, l1, l2, ints, ints1, i, messageChain);
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
		OrangeManager.GLOBAL.broadcast(event, false);
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
		return new FriendX(bot, friendInfo);
	}

	@NotNull
	@Override
	public Stranger newStranger(@NotNull Bot bot, @NotNull StrangerInfo strangerInfo)
	{
		return new StrangerX(bot, strangerInfo.getUin());
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
		_MiraiInstance.set(API);
	}
}
