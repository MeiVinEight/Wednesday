package org.mve.sn.message;

import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;

public class SupernovaMessage implements Message, SingleMessage
{
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA = "data";
	public static final String KEY_TEXT = "text";
	public static final String KEY_FILE = "file";
	public static final String KEY_ID = "id";
	public static final String KEY_SUMMARY = "summary";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_FACE = "face";
	public final Supernova context;
	public final String message;
	public final String content;
	private final Json data;

	public SupernovaMessage(Supernova context, String msg)
	{
		this.context = context;
		this.message = msg;
		this.data = Json.resolve(msg);
		this.content = this.data.string(SupernovaAPI.KEY_RAW_MESSAGE);
	}

	public MessageChain message()
	{
		Json array = this.data.get(SupernovaAPI.KEY_MESSAGE);
		MessageChainBuilder builder = new MessageChainBuilder(1 + array.length());
		for (int i = 0; i < array.length(); i++)
		{
			Json val = array.get(i);
			String type = val.string(KEY_TYPE);
			Json data = val.get(KEY_DATA);
			if (TYPE_TEXT.equals(type))
			{
				PlainText msg = new PlainText(data.string(KEY_TEXT));
				builder.add(msg);
			}
			if (TYPE_IMAGE.equals(type))
			{
				SupernovaImage img = new SupernovaImage(data.string(KEY_FILE));
				img.raw = data.stringify();
				builder.add(img);
			}
			if (TYPE_FACE.equals(type))
			{
				Face face = new Face(Integer.parseInt(data.string(KEY_ID)));
				builder.add(face);
			}
		}
		return builder.build();
	}

	@NotNull
	@Override
	public String contentToString()
	{
		return this.content;
	}

	@Override
	public String toString()
	{
		return this.contentToString();
	}
}
