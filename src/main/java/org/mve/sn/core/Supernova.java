package org.mve.sn.core;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineName;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.contact.friendgroup.FriendGroups;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.internal.AbstractBot;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.data.FriendInfoW;
import org.mve.sn.data.StrangerInfoW;
import org.mve.sn.ws.SupernovaWS;
import org.mve.uni.CompletionWaiting;
import org.mve.uni.Json;
import org.mve.uni.Mirroring;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Supernova extends AbstractBot
{
	public static final MiraiLogger NOP_LOGGER = new SimpleLogger(null, (a, b, c) -> null);
	public static final int ECHO_META_LIFECYCLE_CONNECT = -1;
	private final Map<Integer, CompletionWaiting<Json>> action = new ConcurrentHashMap<>();
	public final BotConfiguration configuration;
	public final MiraiLogger logger;
	private final SupernovaWS connection;
	public final long ID;
	public final String version;
	public Throwable error;
	private boolean active;
	private int echo = 0;

	public Supernova(String url, String token, BotConfiguration configuration)
	{
		this(url, token, configuration, NOPLogger.NOP_LOGGER);
	}

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
			this.version = SupernovaAPI.getVersionInfo(this).stringify();
		}
		catch (Throwable e)
		{
			this.error = e;
			Mirroring.thrown(e);
			// Reachable
			throw new RuntimeException(e);
		}
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
		return null;
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
		return null;
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
		return "";
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
		return Mirroring.checkcast(new CoroutineName("Supernova-QQ"));
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
			System.out.println("Communicate: " + json);
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return null;
		}

		CompletionWaiting<Json> future = new CompletionWaiting<>();
		int echo = this.echo++;
		json.set(SupernovaAPI.KEY_ECHO, echo);
		this.action.put(echo, future);
		System.out.println("Communicate: " + json);
		synchronized (future)
		{
			this.connection.send(json.stringify().getBytes(StandardCharsets.UTF_8));
			return future.get(5, TimeUnit.SECONDS, false);
		}
	}

	public void complete(Json json)
	{
		int echo = Integer.MIN_VALUE;
		if (json.contains(SupernovaAPI.KEY_ECHO))
			echo = json.number(SupernovaAPI.KEY_ECHO).intValue();
		else if (SupernovaAPI.POST_TYPE_META_EVENT.equals(json.string(SupernovaAPI.KEY_POST_TYPE)))
		{
			String metaEvent = json.string(SupernovaAPI.KEY_META_EVENT_TYPE);
			if (SupernovaAPI.META_EVENT_LIFECYCLE.equals(metaEvent) && SupernovaAPI.LIFECYCLE_CONNECT.equals(json.string(SupernovaAPI.KEY_SUBTYPE)))
				echo = -1;
		}
		CompletionWaiting<Json> waiting = this.action.remove(echo);
		if (waiting != null)
			waiting.complete(json);
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
	}
}
