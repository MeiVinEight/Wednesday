package org.mve.orange.message;

import kotlin.coroutines.Continuation;
import net.mamoe.mirai.contact.FileSupported;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.message.data.FileMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class WrappedFileMessage implements FileMessage, MessageJson
{
	public final String ID;
	public final String name;
	public final String url;
	public final LazyJVM<Long> size;

	public WrappedFileMessage(String id, String name, String url)
	{
		ID = id;
		this.name = name;
		this.url = url;
		size = new LazyJVM<>(() -> 0L);
	}

	@NotNull
	@Override
	public String getId()
	{
		return this.ID;
	}

	@Override
	public int getInternalId()
	{
		return 0;
	}

	@NotNull
	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public long getSize()
	{
		return this.size.getValue();
	}

	@Nullable
	@Override
	public AbsoluteFile toAbsoluteFile(@NotNull FileSupported fileSupported, @NotNull Continuation<? super AbsoluteFile> continuation)
	{
		return fileSupported.getFiles().getRoot().resolveFileById(this.getId());
	}

	@NotNull
	@Override
	public String contentToString()
	{
		return "[文件]" + this.name;
	}

	@Override
	public Json json()
	{
		return new Json()
			.set(OrangeMessage.KEY_TYPE, OrangeMessage.TYPE_FILE)
			.set(OrangeMessage.KEY_DATA, new Json()
				.set(OrangeMessage.KEY_FILE, this.name)
				.set(OrangeMessage.KEY_FILE_ID, this.ID)
				.set(OrangeMessage.KEY_URL, this.url)
			);
	}
}
