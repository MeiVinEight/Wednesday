package org.mve.sn.core;

import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlinx.coroutines.CoroutineName;
import kotlinx.coroutines.Dispatchers;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.contact.friendgroup.FriendGroups;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.internal.AbstractBot;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.asm.*;
import org.mve.asm.attribute.CodeWriter;
import org.mve.invoke.MagicAccessor;
import org.mve.invoke.common.JavaVM;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.coroutine.ContinuationX;
import org.mve.sn.coroutine.CoroutineDispatcherX;
import org.mve.sn.coroutine.CoroutineX;
import org.mve.sn.data.FriendInfoW;
import org.mve.sn.data.StrangerInfoW;
import org.mve.sn.event.SupernovaManager;
import org.mve.sn.ws.SupernovaWS;
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

public class Supernova extends AbstractBot
{
	public static final MiraiLogger NOP_LOGGER = new SimpleLogger(null, (a, b, c) -> null);
	public static final int ECHO_META_LIFECYCLE_CONNECT = -1;
	public static final CoroutineContext COROUTINE_CONTEXT = Mirroring.checkcast(new CoroutineX());
	public static final Continuation<? super Object> CONTINUATION = new ContinuationX<>(Supernova.COROUTINE_CONTEXT);
	private final Map<Integer, CompletionWaiting<Json>> action = new ConcurrentHashMap<>();
	public final BotConfiguration configuration;
	public final MiraiLogger logger;
	private final SupernovaWS connection;
	public final long ID;
	public final String version;
	public Throwable error;
	private boolean active;
	private int echo = 0;
	private final Lazy<String> nickname;
	private final Lazy<ContactList<Friend>> friends;
	private final Lazy<EventChannel<BotEvent>> channel;

	public Supernova(String url, String token, BotConfiguration configuration, Logger wsLogger)
	{
		this.configuration = configuration;
		try
		{
			this.connection = new SupernovaWS(this, new URI(url), token, wsLogger);
			this.connection.connect();
			CompletionWaiting<Json> metaWait = new CompletionWaiting<>();
			this.action.put(ECHO_META_LIFECYCLE_CONNECT, metaWait);
			Json meta = metaWait.get();
			this.ID = meta.number(SupernovaAPI.KEY_SELF_ID).longValue();
			this.logger = configuration.getBotLoggerSupplier().invoke(this);
			SupernovaManager.GLOBAL.broadcast(new BotOnlineEvent(this));
			Json version = SupernovaAPI.getVersionInfo(this).get(SupernovaAPI.KEY_DATA);
			this.version = version.stringify();
			this.getLogger().info(version.string(SupernovaAPI.KEY_APP_NAME) + ": " + version.string(SupernovaAPI.KEY_APP_VERSION));
		}
		catch (Throwable e)
		{
			this.error = e;
			Mirroring.thrown(e);
			// Reachable
			throw new RuntimeException(e);
		}
		this.nickname = LazyKt.lazy(() -> SupernovaAPI.getLoginInfo(this).get(SupernovaAPI.KEY_DATA).string(SupernovaAPI.KEY_NICKNAME));
		this.friends = LazyKt.lazy(() -> {
			ContactList<Friend> list = new ContactList<>(new CopyOnWriteArraySet<>());
			Json api = SupernovaAPI.getFriendList(this);
			Json data = api.get(SupernovaAPI.KEY_DATA);
			for (int i = 0; i < data.length(); i++)
			{
				Json friend = data.get(i);
				long id = friend.number(SupernovaAPI.KEY_USER_ID).longValue();
				String nick = friend.string(SupernovaAPI.KEY_NICKNAME);
				String remark = friend.string(SupernovaAPI.KEY_REMARK);
				FriendInfoW info = new FriendInfoW(id, 0, nick, remark);
				list.add(Mirai.getInstance().newFriend(this, info));
			}
			return list;
		});
		this.channel = LazyKt.lazy(() -> GlobalEventChannel.INSTANCE.filterIsInstance(BotEvent.class).filter(e -> e.getBot() == this));
	}

	@NotNull
	@Override
	public Friend getAsFriend()
	{
		return Mirai.getInstance().newFriend(this, new FriendInfoW(this.getId(), 0, null, null));
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
		return Mirai.getInstance().newStranger(this, new StrangerInfoW(this.getId(), 0, null, null));
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
		return this.friends.getValue();
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
		return null;
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
		this.error(throwable);
		this.connection.close();
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
		return Supernova.COROUTINE_CONTEXT;
	}

	public void open(ServerHandshake handshakedata)
	{
		this.active = true;
		synchronized (this)
		{
			this.notifyAll();
		}
	}

	public void message(Json json)
	{
		this.complete(json);
	}

	public void error(Throwable ex)
	{
		if (ex != null)
			this.connection.logger.error("未知错误", ex);
		this.error = ex;
	}

	public void close(int code, String reason, boolean remote)
	{
		System.out.println("Close: code=" + code + " reason=" + reason + " remote=" + remote);
		this.active = false;
	}

	public Json communicate(Json json, boolean async)
	{
		if (async)
		{
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return null;
		}

		CompletionWaiting<Json> future = new CompletionWaiting<>();
		int echo = this.echo++;
		json.set(SupernovaAPI.KEY_ECHO, echo);
		this.action.put(echo, future);
		synchronized (future)
		{
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return future.get(11, TimeUnit.SECONDS, false);
		}
	}

	public void complete(Json json)
	{
		int echo = Integer.MIN_VALUE;
		if (json.contains(SupernovaAPI.KEY_ECHO))
			echo = json.number(SupernovaAPI.KEY_ECHO).intValue();
		String failedMsg = null;
		if (json.contains(SupernovaAPI.KEY_STATUS) && (new Json(SupernovaAPI.STATUS_FAILED)).equals(json.get(SupernovaAPI.KEY_STATUS)))
		{
			StringBuilder builder = new StringBuilder("FAILED ");
			if (echo != Integer.MIN_VALUE)
				builder.append('[').append(echo).append("]");
			builder.append('[')
				.append(json.number(SupernovaAPI.KEY_RETCODE))
				.append("] ")
				.append(failedMsg = json.string(SupernovaAPI.KEY_MESSAGE));
			this.getLogger().error(builder.toString());
		}
		else if (SupernovaAPI.POST_TYPE_META_EVENT.equals(json.string(SupernovaAPI.KEY_POST_TYPE)))
		{
			String metaEvent = json.string(SupernovaAPI.KEY_META_EVENT_TYPE);
			if (SupernovaAPI.META_EVENT_LIFECYCLE.equals(metaEvent) && SupernovaAPI.LIFECYCLE_CONNECT.equals(json.string(SupernovaAPI.KEY_SUBTYPE)))
				echo = -1;
		}
		CompletionWaiting<Json> waiting = this.action.remove(echo);
		if (waiting != null)
		{
			if (failedMsg != null)
				waiting.exception(new APIException(failedMsg));
			waiting.complete(json);
		}
	}

	static
	{
		try
		{
			Class.forName("org.mve.sn.SupernovaQQ");
		}
		catch (ClassNotFoundException e)
		{
			Mirroring.thrown(e);
		}

		LazyJVM<EventChannel<Event>> lazy = new LazyJVM<>(() -> SupernovaManager.GLOBAL);
		Mirroring.set(GlobalEventChannel.class, "instance$delegate", Lazy.class, lazy);

		//net/mamoe/mirai/event/EventChannel$subscribeOnce$2


		Function4<String, String, String, Integer, Unit> sub = (typeName, fieldName, fieldDesc, type) -> {
			ClassWriter cw = new ClassWriter()
				.set(Opcodes.version(8), AccessFlag.PUBLIC, typeName, "java/lang/Object", "kotlin/jvm/functions/Function2")
				.field(new FieldWriter()
					.set(AccessFlag.PRIVATE | AccessFlag.FINAL, fieldName, fieldDesc)
				)
				.method(new MethodWriter()
					.set(AccessFlag.PUBLIC, "<init>", "(Ljava/util/function/Consumer;Lkotlin/coroutines/Continuation;)V")
					.attribute(new CodeWriter()
						.instruction(Opcodes.ALOAD_0)
						.method(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
						.instruction(Opcodes.ALOAD_0)
						.instruction(Opcodes.ALOAD_1)
						.field(Opcodes.PUTFIELD, typeName, fieldName, fieldDesc)
						.instruction(Opcodes.RETURN)
						.stack(2)
						.local(3)
					)
				)
				.method(new MethodWriter()
					.set(AccessFlag.PUBLIC, "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
					.attribute(new CodeWriter()
						.instruction(Opcodes.ALOAD_0)
						.field(Opcodes.GETFIELD, typeName, fieldName, fieldDesc)
						.instruction(Opcodes.ALOAD_1)
						.method(Opcodes.INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true)
						.field(Opcodes.GETSTATIC, "net/mamoe/mirai/event/ListeningStatus", (type == 0) ? "LISTENING" : "STOPPED", "Lnet/mamoe/mirai/event/ListeningStatus;")
						.instruction(Opcodes.ARETURN)
						.stack(2)
						.local(3)
					)
				);
			byte[] bytes = cw.toByteArray();
			MagicAccessor.accessor.defineClass(Supernova.class.getClassLoader(), bytes);
			return null;
		};
		String typeName = "net/mamoe/mirai/event/EventChannel$subscribeAlways$2";
		String fieldName = JavaVM.random();
		String fieldDesc = "Ljava/util/function/Consumer;";
		sub.invoke(typeName, fieldName, fieldDesc, 0);
		typeName = "net/mamoe/mirai/event/EventChannel$subscribeOnce$2";
		sub.invoke(typeName, fieldName, fieldDesc, 1);
		typeName = "net/mamoe/mirai/event/EventChannel$subscribe$2";
		fieldDesc = "Ljava/util/function/Function;";
		ClassWriter cw = new ClassWriter()
			.set(Opcodes.version(8), AccessFlag.PUBLIC, typeName, "java/lang/Object", "kotlin/jvm/functions/Function2")
			.field(new FieldWriter()
				.set(AccessFlag.PRIVATE | AccessFlag.FINAL, fieldName, fieldDesc)
			)
			.method(new MethodWriter()
				.set(AccessFlag.PUBLIC, "<init>", "(Ljava/util/function/Function;Lkotlin/coroutines/Continuation;)V")
				.attribute(new CodeWriter()
					.instruction(Opcodes.ALOAD_0)
					.method(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
					.instruction(Opcodes.ALOAD_0)
					.instruction(Opcodes.ALOAD_1)
					.field(Opcodes.PUTFIELD, typeName, fieldName, fieldDesc)
					.instruction(Opcodes.RETURN)
					.stack(2)
					.local(3)
				)
			)
			.method(new MethodWriter()
				.set(AccessFlag.PUBLIC, "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
				.attribute(new CodeWriter()
					.instruction(Opcodes.ALOAD_0)
					.field(Opcodes.GETFIELD, typeName, fieldName, fieldDesc)
					.instruction(Opcodes.ALOAD_1)
					.method(Opcodes.INVOKEINTERFACE, "java/util/function/Function", "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", true)
					.instruction(Opcodes.ARETURN)
					.stack(2)
					.local(3)
				)
			);
		byte[] bytes = cw.toByteArray();
		MagicAccessor.accessor.defineClass(Supernova.class.getClassLoader(), bytes);
		//Mirroring.set(Dispatchers.class, "IO", new CoroutineDispatcherX());
		//Dispatchers.getIO()
	}
}
