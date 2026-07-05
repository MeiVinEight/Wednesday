package org.mve.orange.core.contact;

import kotlin.Lazy;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.GroupSettings;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.active.GroupActive;
import net.mamoe.mirai.contact.announcement.Announcements;
import net.mamoe.mirai.contact.essence.Essences;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.contact.roaming.RoamingMessages;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.OnlineMessageSource;
import net.mamoe.mirai.utils.RemoteFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.APIResponse;
import org.mve.orange.core.Orange;
import org.mve.orange.data.SourceToGroup;
import org.mve.orange.event.OrangeManager;
import org.mve.orange.message.MessageJson;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class GroupX extends ContactX implements Group
{
	private final Orange context;
	private final Lazy<GroupActive> active;
	private final Lazy<Json> info;
	private final Lazy<String> name;

	public GroupX(Orange context, long id)
	{
		super(context, id);
		this.context = context;
		this.active = new LazyJVM<>(() -> new GroupActiveX(GroupX.this.context, GroupX.this.getId()));
		this.info = new LazyJVM<>(() ->
		{
			APIResponse response = OrangeAPI.getGroupInfo(this.context, this.getId(), false);
			response.checkValidation();
			return response.data;
		});
		this.name = new LazyJVM<>(() -> this.info.getValue().string(OrangeAPI.KEY_GROUP_NAME));
	}

	@NotNull
	@Override
	public GroupActive getActive()
	{
		return this.active.getValue();
	}

	@NotNull
	@Override
	public String getName()
	{
		return this.name.getValue();
	}

	@Override
	public void setName(@NotNull String s)
	{
	}

	@NotNull
	@Override
	public GroupSettings getSettings()
	{
		return null;
	}

	@NotNull
	@Override
	public NormalMember getOwner()
	{
		return null;
	}

	@NotNull
	@Override
	public NormalMember getBotAsMember()
	{
		return new MemberX(this.context, this.context.getId(), this.getId());
	}

	@NotNull
	@Override
	public ContactList<NormalMember> getMembers()
	{
		return null;
	}

	@NotNull
	@Override
	public Announcements getAnnouncements()
	{
		return null;
	}

	@Nullable
	@Override
	public NormalMember get(long l)
	{
		return null;
	}

	@Override
	public boolean contains(long l)
	{
		return false;
	}

	@Nullable
	@Override
	public Object quit(@NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	@SuppressWarnings("rawtypes")
	public MessageReceipt sendMessage(@NotNull Message message, @NotNull Continuation continuation)
	{
		GroupMessagePreSendEvent preSend = new GroupMessagePreSendEvent(this, message);
		OrangeManager.GLOBAL.broadcast(preSend, true);
		if (preSend.isCancelled())
			throw new EventCancelledException();
		if (this.getBotAsMember().isMuted())
			throw new BotIsBeingMutedException(this, message);
		message = preSend.getMessage();

		if (!(message instanceof MessageChain))
			message = new MessageChainBuilder().append(message).build();
		Json msg = ((MessageJson) message).json();
		int time = (int) (System.currentTimeMillis() / 1000);
		APIResponse response = OrangeAPI.sendGroupMessage(this.context, this.getId(), msg, false);
		try
		{
			response.checkValidation();
		}
		catch (Throwable e)
		{
			OrangeManager.GLOBAL.broadcast(new GroupMessagePostSendEvent(this, (MessageChain) message, e, null), false);
			throw e;
		}
		int id = response.data.number(OrangeAPI.KEY_MESSAGE_ID).intValue();
		OnlineMessageSource.Outgoing outgoing = new SourceToGroup(this.context, this, id, time, (MessageChain) message);
		MessageReceipt<Group> receipt = new MessageReceipt<>(outgoing, this);
		OrangeManager.GLOBAL.broadcast(new GroupMessagePostSendEvent(this, (MessageChain) message, null, receipt), false);
		return receipt;
	}

	@Nullable
	@Override
	public Object setEssenceMessage(@NotNull MessageSource messageSource, @NotNull Continuation<? super Boolean> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Essences getEssences()
	{
		return null;
	}

	@NotNull
	@Override
	public RemoteFile getFilesRoot()
	{
		return null;
	}

	@NotNull
	@Override
	public RemoteFiles getFiles()
	{
		return null;
	}

	@NotNull
	@Override
	public RoamingMessages getRoamingMessages()
	{
		return null;
	}
}
