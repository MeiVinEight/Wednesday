package org.mve.sn.core.contact;

import kotlin.Lazy;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.GroupSettings;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.active.GroupActive;
import net.mamoe.mirai.contact.announcement.Announcements;
import net.mamoe.mirai.contact.essence.Essences;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.contact.roaming.RoamingMessages;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.RemoteFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.sn.core.Supernova;
import org.mve.uni.LazyJVM;

public class SupernovaGroup extends SupernovaContact implements Group
{
	private final Supernova context;
	private final Lazy<GroupActive> active;

	public SupernovaGroup(Supernova context, long id)
	{
		super(context, id);
		this.context = context;
		this.active = new LazyJVM<>(() -> new GroupActiveWrapper(SupernovaGroup.this.context, SupernovaGroup.this.getId()));
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
		return "";
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
		return null;
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
		return null;
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
