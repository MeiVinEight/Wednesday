package org.mve.orange.core;

import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.contact.friendgroup.FriendGroups;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.MiraiLogger;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.contact.GroupX;
import org.mve.orange.core.contact.MemberX;
import org.mve.orange.coroutine.ContinuationX;
import org.mve.orange.coroutine.CoroutineX;
import org.mve.orange.data.WrappedFriendInfo;
import org.mve.orange.data.SourceFromFriend;
import org.mve.orange.data.SourceFromGroup;
import org.mve.orange.data.WrappedStrangerInfo;
import org.mve.orange.event.HeartbeatEvent;
import org.mve.orange.event.PostingEvent;
import org.mve.orange.event.PostingMessageEvent;
import org.mve.orange.event.OrangeEvent;
import org.mve.orange.message.OrangeMessage;
import org.mve.orange.ws.OrangeWS;
import org.mve.uni.CompletionWaiting;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;
import org.mve.uni.Mirroring;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class Orange implements Bot
{
	public static final MiraiLogger NOP_LOGGER = MiraiLogger.Factory.INSTANCE.create(Orange.class, "0");
	public static final int ECHO_META_LIFECYCLE_CONNECT = -1;
	public static final CoroutineContext COROUTINE_CONTEXT = Mirroring.checkcast(new CoroutineX());
	public static final Continuation<? super Object> CONTINUATION = new ContinuationX<>(Orange.COROUTINE_CONTEXT);
	private final Map<Integer, CompletionWaiting<APIResponse>> action = new ConcurrentHashMap<>();
	public final BotConfiguration configuration;
	private OrangeWS connection;
	public MiraiLogger logger;
	public long ID;
	public String version;
	public Throwable error;
	private boolean active;
	private int echo = 0;
	private final Lazy<String> nickname = LazyKt.lazy(() -> OrangeAPI.getLoginInfo(this).data.string(OrangeAPI.KEY_NICKNAME));
	private final Lazy<CopyOnWriteArraySet<Friend>> friends;
	private final Lazy<ConcurrentHashMap<Long, Group>> groups;
	private final Lazy<EventChannel<BotEvent>> channel = LazyKt.lazy(() -> GlobalEventChannel.INSTANCE.filterIsInstance(BotEvent.class).filter(e -> e.getBot() == this));
	private APIResponse failure = null;
	private final Lazy<Friend> friend = new LazyJVM<>(() -> Mirai.getInstance().newFriend(this, new WrappedFriendInfo(this.getId(), 0, this.nickname.getValue(), null)));
	private final MessageArray message = new MessageArray();

	public Orange(String url, String token, BotConfiguration configuration, Logger wsLogger)
	{
		this.configuration = configuration;
		this.friends = LazyKt.lazy(() -> {
			CopyOnWriteArraySet<Friend> list = new CopyOnWriteArraySet<>();
			APIResponse api = OrangeAPI.getFriendList(this);
			api.checkValidation();
			Json data = api.data;
			for (int i = 0; i < data.length(); i++)
			{
				Json friend = data.get(i);
				long id = friend.number(OrangeAPI.KEY_USER_ID).longValue();
				String nick = friend.string(OrangeAPI.KEY_NICKNAME);
				String remark = friend.string(OrangeAPI.KEY_REMARK);
				WrappedFriendInfo info = new WrappedFriendInfo(id, 0, nick, remark);
				list.add(Mirai.getInstance().newFriend(this, info));
			}
			return list;
		});
		this.groups = new LazyJVM<>(() ->
		{
			APIResponse response = OrangeAPI.getGroupList(this);
			response.checkValidation();
			Json data = response.data;
			ConcurrentHashMap<Long, Group> map = new ConcurrentHashMap<>();
			for (int i = 0; i < data.length(); i++)
			{
				Json info = data.get(i);
				long id = info.number(OrangeAPI.KEY_GROUP_ID).longValue();
				map.put(id, new GroupX(this, id));
			}
			return map;
		});
		try
		{
			this.connection = new OrangeWS(this, new URI(url), token, wsLogger);
			this.connection.connect();
			CompletionWaiting<APIResponse> metaWait = new CompletionWaiting<>();
			this.action.put(ECHO_META_LIFECYCLE_CONNECT, metaWait);
			APIResponse response = metaWait.get();
			if (response.status != APIResponse.STATUS_OK)
			{
				this.failure = response;
				this.close(response.message, null);
				return;
			}
			this.active = true;
			Json meta = response.origin;
			this.ID = meta.number(OrangeAPI.KEY_SELF_ID).longValue();
			this.logger = configuration.getBotLoggerSupplier().invoke(this);
			Orange old = OrangeAPI.BOT.get(this.ID);
			if (old != null && old.isOnline())
			{
				this.close("重复连接相同账号", null);
				return;
			}
			OrangeAPI.BOT.put(this.ID, this);
			OrangeEvent.GLOBAL.broadcast(new BotOnlineEvent(this), false);
			Json version = OrangeAPI.getVersionInfo(this).data;
			this.version = version.stringify();
			this.getLogger().info(version.string(OrangeAPI.KEY_APP_NAME) + ": " + version.string(OrangeAPI.KEY_APP_VERSION));
		}
		catch (Throwable e)
		{
			this.close(e);
		}
		this.getEventChannel().subscribeAlways(MessageEvent.class, this.message);
	}

	@NotNull
	@Override
	public Friend getAsFriend()
	{
		return this.friend.getValue();
	}

	@NotNull
	@Override
	public BotConfiguration getConfiguration()
	{
		return this.configuration;
	}

	@NotNull
	@Override
	public MiraiLogger getLogger()
	{
		if (this.logger == null)
			return NOP_LOGGER;
		return this.logger;
	}

	@Override
	public boolean isOnline()
	{
		return this.active;
	}

	@NotNull
	@Override
	public EventChannel<BotEvent> getEventChannel()
	{
		return this.channel.getValue();
	}

	@NotNull
	@Override
	public ContactList<OtherClient> getOtherClients()
	{
		return null;
	}

	@NotNull
	@Override
	public Stranger getAsStranger()
	{
		return Mirai.getInstance().newStranger(this, new WrappedStrangerInfo(this.getId(), 0, null, null));
	}

	@Nullable
	@Override
	public Stranger getStranger(long id)
	{
		return Mirai.getInstance().newStranger(this, new WrappedStrangerInfo(id, 0, null, null));
	}

	@NotNull
	@Override
	public ContactList<Stranger> getStrangers()
	{
		return null;
	}

	@NotNull
	@Override
	public ContactList<Friend> getFriends()
	{
		return new ContactList<>(this.friends.getValue());
	}

	@NotNull
	@Override
	public FriendGroups getFriendGroups()
	{
		return null;
	}

	@NotNull
	@Override
	public ContactList<Group> getGroups()
	{
		return new ContactList<>(this.groups.getValue().values());
	}

	@NotNull
	@Override
	public Group getGroup(long id)
	{
		Group grp = this.groups.getValue().get(id);
		if (grp == null)
			grp = new GroupX(this, id);
		return grp;
	}

	@Nullable
	@Override
	public Object login(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Override
	public void close(@Nullable Throwable throwable)
	{
		this.close(null, throwable);
	}

	@NotNull
	@Override
	public String getNick()
	{
		return this.nickname.getValue();
	}

	@Override
	public long getId()
	{
		return this.ID;
	}

	@NotNull
	@Override
	public CoroutineContext getCoroutineContext()
	{
		return Orange.COROUTINE_CONTEXT;
	}

	public void close(String msg, Throwable throwable)
	{
		if (throwable != null)
		{
			this.error(throwable);
			if (msg == null)
				msg = throwable.toString();
		}
		if (msg == null)
			msg = "主动断开";
		this.connection.close(CloseFrame.NORMAL, msg);
	}

	public void open(ServerHandshake handshakedata)
	{
		synchronized (this)
		{
			this.notifyAll();
		}
	}

	public void message(Json json)
	{
		if (!json.contains(OrangeAPI.KEY_POST_TYPE))
		{
			this.complete(json);
			return;
		}

		String postType = json.string(OrangeAPI.KEY_POST_TYPE);
		if (OrangeAPI.POST_TYPE_META_EVENT.equals(postType))
		{
			String metaEventType = json.string(OrangeAPI.KEY_META_EVENT_TYPE);
			if (OrangeAPI.META_EVENT_LIFECYCLE.equals(metaEventType))
			{
				String subType = json.string(OrangeAPI.KEY_SUBTYPE);
				if (OrangeAPI.LIFECYCLE_CONNECT.equals(subType))
				{
					json.set(OrangeAPI.KEY_ECHO, ECHO_META_LIFECYCLE_CONNECT)
						.set(OrangeAPI.KEY_STATUS, OrangeAPI.STATUS_OK);
					this.complete(json);
				}
			}
			if (OrangeAPI.META_EVENT_HEARTBEAT.equals(metaEventType))
			{
				HeartbeatEvent event = new HeartbeatEvent(this, json);
				OrangeEvent.GLOBAL.broadcast(event, false);
			}
		}
		if (postType != null)
			OrangeEvent.GLOBAL.broadcast(new PostingEvent(this, json.stringify()), false);
	}

	public Throwable error(Throwable ex)
	{
		Throwable old = this.error;
		if (ex != null)
			this.getLogger().error("未知错误", ex);
		this.error = ex;
		return old;
	}

	public void close(int code, String reason, boolean remote)
	{
		if (this.failure != null && this.failure.message != null)
			reason = this.failure.message;
		if (reason == null && remote)
			reason = "远程连接断开";
		if (reason == null)
			reason = "未知原因";
		this.getLogger().info("[" + code + "] 服务器连接因 " + reason + " 已关闭");
		this.active = false;
		this.action.forEach((echo, waiting) -> waiting.complete(new APIResponse(new Json()
			.set(OrangeAPI.KEY_STATUS, OrangeAPI.STATUS_FAILED)
			.set(OrangeAPI.KEY_RETCODE, 1200)
			.set(OrangeAPI.KEY_ECHO, echo.intValue())
			.set(OrangeAPI.KEY_MESSAGE, "连接已断开")
		)));
		if (remote)
			OrangeEvent.GLOBAL.broadcast(new BotOfflineEvent.Dropped(this, null), false);
		else
			OrangeEvent.GLOBAL.broadcast(new BotOfflineEvent.Active(this, null), false);
	}

	public APIResponse communicate(Json json, boolean async)
	{
		if (async)
		{
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return null;
		}

		CompletionWaiting<APIResponse> future = new CompletionWaiting<>();
		int echo = this.echo++;
		json.set(OrangeAPI.KEY_ECHO, echo);
		this.action.put(echo, future);
		synchronized (future)
		{
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return future.get(11, TimeUnit.SECONDS, false);
		}
	}

	public MessageSource source(int id)
	{
		return this.message.get(id);
	}

	private void complete(Json json)
	{
		this.failure = null;
		APIResponse response = new APIResponse(json);
		Number echo = response.echo;
		if (response.status == APIResponse.STATUS_FAILED)
		{
			this.failure = response;
			StringBuilder builder = new StringBuilder("FAILED ");
			if (echo != null)
				builder.append('[').append(echo).append("]");
			builder.append('[')
				.append(response.code)
				.append("] ")
				.append(response.message);
			this.getLogger().error(builder.toString());
		}
		if (echo == null)
			return;
		CompletionWaiting<APIResponse> waiting = this.action.remove(echo.intValue());
		if (waiting != null)
			waiting.complete(response);
	}

	static
	{
		try
		{
			Class.forName("org.mve.orange.OrangeAPI");
		}
		catch (ClassNotFoundException e)
		{
			Mirroring.thrown(e);
		}


		//Mirroring.set(Dispatchers.class, "IO", new CoroutineDispatcherX());
		//Dispatchers.getIO()

		OrangeEvent.GLOBAL.subscribeAlways(PostingEvent.class, (e) ->
		{
			String postType = e.type;
			if (OrangeAPI.POST_TYPE_MESSAGE.equals(postType))
			{
				OrangeEvent.GLOBAL.broadcast(new PostingMessageEvent(e.context, e.text), false);
			}
		});
		OrangeEvent.GLOBAL.subscribeAlways(PostingMessageEvent.class, e -> {
			if (OrangeAPI.MESSAGE_TYPE_PRIVATE.equals(e.type))
			{
				long fid = e.origin.get(OrangeAPI.KEY_SENDER).number(OrangeAPI.KEY_USER_ID).longValue();
				Friend friend = e.getBot().getFriend(fid);
				if (friend == null)
					friend = Mirai.getInstance().newFriend(e.getBot(), new WrappedFriendInfo(fid, 0, null, null));
				OrangeEvent.GLOBAL.broadcast(new FriendMessageEvent(
					friend,
					new SourceFromFriend(e.context, e.text).plus(new OrangeMessage(e.context, e.text).message())/**/,
					(int) e.time
				), false);
			}
			if (OrangeAPI.MESSAGE_TYPE_GROUP.equals(e.type))
			{
				Json sender = e.origin.get(OrangeAPI.KEY_SENDER);
				String role = sender.string(OrangeAPI.KEY_ROLE);
				MemberPermission perm = OrangeAPI.permission(role);
				if (perm == null)
				{
					e.getBot().getLogger().warning("未知的群成员类型: " + role);
					return;
				}
				long fid = e.origin.number(OrangeAPI.KEY_USER_ID).longValue();
				String card = sender.string(OrangeAPI.KEY_CARD);
				if (card == null || card.isEmpty())
					card = sender.string(OrangeAPI.KEY_NICKNAME);
				long gid = e.origin.number(OrangeAPI.KEY_GROUP_ID).longValue();
				MessageSource source = new SourceFromGroup(e.context, e.text, gid, fid);
				MessageChain chain = source.plus(new OrangeMessage(e.context, e.text).message());
				MemberX member = new MemberX(e.context, fid, gid, perm);
				OrangeEvent.GLOBAL.broadcast(new GroupMessageEvent(card, perm, member, chain, (int) e.time), false);
			}
		});
	}
}
