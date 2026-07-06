package org.mve.orange.core.contact;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.active.MemberActive;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.APIResponse;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

import java.util.Arrays;

public class MemberX extends UserX implements NormalMember
{
	private final Orange context;
	private final long group;
	private final LazyJVM<Json> info;
	private final LazyJVM<MemberPermission> permission;

	public MemberX(Orange context, long id, long group)
	{
		super(context, id);
		this.context = context;
		this.group = group;
		this.info = new LazyJVM<>(() -> {
			APIResponse response = OrangeAPI.getGroupMemberInfo(MemberX.this.context, this.group, this.getId(), false);
			response.checkValidation();
			return response.data;
		});
		this.permission = new LazyJVM<>(() -> {
			Json info = this.info.getValue();
			return OrangeAPI.permission(info.string(OrangeAPI.KEY_ROLE));
		});
	}

	public MemberX(Orange context, long id, long group, MemberPermission permission)
	{
		this(context, id, group);
		this.permission.setValue(permission);
	}

	@NotNull
	@Override
	public String getNameCard()
	{
		return "";
	}

	@Override
	public void setNameCard(@NotNull String s)
	{
	}

	@NotNull
	@Override
	public String getSpecialTitle()
	{
		return "";
	}

	@Override
	public void setSpecialTitle(@NotNull String s)
	{
	}

	@Override
	public int getMuteTimeRemaining()
	{
		APIResponse response = OrangeAPI.getGroupMemberInfo(this.context, this.group, this.getId(), false);
		response.checkValidation();
		int time = response.data.number(OrangeAPI.KEY_SHUT_UP_TIMESTAMP).intValue();
		int now = Math.toIntExact(System.currentTimeMillis() / 1000);
		if (now > time)
			return 0;
		return time - now;
	}

	@Override
	public int getJoinTimestamp()
	{
		return 0;
	}

	@Override
	public int getLastSpeakTimestamp()
	{
		return 0;
	}

	@Nullable
	@Override
	public Object unmute(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object kick(@NotNull String s, boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object modifyAdmin(boolean b, @NotNull Continuation<? super Unit> continuation)
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
	@SuppressWarnings("rawtypes")
	public MessageReceipt sendMessage(@NotNull String message, @NotNull Continuation continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Group getGroup()
	{
		return this.context.getGroup(this.group);
	}

	@NotNull
	@Override
	public MemberPermission getPermission()
	{
		return this.permission.getValue();
	}

	@NotNull
	@Override
	public MemberActive getActive()
	{
		return null;
	}

	@Nullable
	@Override
	public Object mute(int i, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof MemberX memberX)) return false;
		return this.group == memberX.group && this.getId() == memberX.getId();
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(new long[]{this.group, this.getId()});
	}
}
