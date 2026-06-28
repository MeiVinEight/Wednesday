package org.mve.sn.message;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;
import org.jetbrains.annotations.NotNull;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public class WrappedImage implements Image, MessageJson
{
	public String raw;
	public final LazyJVM<Json> message;
	public final String file;
	public final LazyJVM<String> summary;

	public WrappedImage(String file)
	{
		this.file = file;
		this.message = new LazyJVM<>(() -> {
			if (this.raw != null)
				return Json.resolve(this.raw);
			return null;
		});
		this.summary = new LazyJVM<>(() -> {
			Json msg = this.message.getValue();
			if (msg == null)
				return "";
			return msg.string(SupernovaMessage.KEY_SUMMARY);
		});
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
		String summ = this.summary.getValue();
		if (summ == null || summ.isEmpty())
			summ = "图片消息";
		return '[' + summ + ']';
	}

	@Override
	public Json json()
	{
		String summ = this.summary.getValue();
		if (summ == null)
			summ = "";
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_IMAGE)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_FILE, this.file)
				.set(SupernovaMessage.KEY_SUMMARY, summ)
			);
	}
}
