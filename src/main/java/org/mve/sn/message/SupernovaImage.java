package org.mve.sn.message;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;
import org.jetbrains.annotations.NotNull;

public class SupernovaImage implements Image
{
	public String raw;
	public final String file;

	public SupernovaImage(String file)
	{
		this.file = file;
	}

	@Override
	public int getHeight()
	{
		return 0;
	}

	@NotNull
	@Override
	public String getImageId()
	{
		return "";
	}

	@Override
	public int getWidth()
	{
		return 0;
	}

	@Override
	public long getSize()
	{
		return 0;
	}

	@NotNull
	@Override
	public ImageType getImageType()
	{
		return ImageType.UNKNOWN;
	}

	@Override
	public void appendMiraiCodeTo(@NotNull StringBuilder builder)
	{
		builder.append("[mirai:image:").append(this.file).append("]");
	}

	@NotNull
	@Override
	public String contentToString()
	{
		return "";
	}
}
