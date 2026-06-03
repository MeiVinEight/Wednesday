package org.mve.sn.core.contact;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.message.data.ShortVideo;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.sn.core.Supernova;
import org.mve.uni.Mirroring;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class SupernovaContact implements Contact
{
	public final Bot context;
	public final long ID;

	public SupernovaContact(Bot context, long id)
	{
		this.context = context;
		this.ID = id;
	}

	@Override
	public long getId()
	{
		return this.ID;
	}

	@NotNull
	@Override
	public Bot getBot()
	{
		return this.context;
	}

	@Nullable
	public MessageReceipt sendMessage(String s)
	{
		return (MessageReceipt<?>) this.sendMessage(s, Mirroring.checkcast(Supernova.CONTINUATION));
	}

	@Nullable
	public MessageReceipt sendMessage(Message s)
	{
		return (MessageReceipt<?>)  this.sendMessage(s, Mirroring.checkcast(Supernova.CONTINUATION));
	}

	@NotNull
	@Override
	public CoroutineContext getCoroutineContext()
	{
		return this.getBot().getCoroutineContext();
	}

	@Nullable
	@Override
	public Image uploadImage(@NotNull ExternalResource externalResource, @NotNull Continuation<? super Image> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public ShortVideo uploadShortVideo(@NotNull ExternalResource externalResource, @NotNull ExternalResource externalResource1, @Nullable String s, @NotNull Continuation<? super ShortVideo> continuation)
	{
		return null;
	}

	@Nullable
	public Object uploadAudio(@NotNull ExternalResource externalResource, @NotNull Continuation<? super OfflineAudio> continuation)
	{
		return null;
	}
}
