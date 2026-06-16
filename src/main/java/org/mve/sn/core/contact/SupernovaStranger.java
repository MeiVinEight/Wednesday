package org.mve.sn.core.contact;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupernovaStranger extends SupernovaUser implements Stranger
{
	public SupernovaStranger(Bot context, long id)
	{
		super(context, id);
	}

	@Nullable
	@Override
	public Object delete(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Nullable
	@Override
	public MessageReceipt<Stranger> sendMessage(@NotNull Message message, @NotNull Continuation continuation)
	{
		return null;
	}
}
