package org.mve.sn.message;

import kotlin.Lazy;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SupernovaMessage implements Message, SingleMessage
{
	private static final Map<String, Function<Json, SingleMessage>> DESERIALIZERS = new HashMap<>();
	private static final Lazy<Function<Json, SingleMessage>> UNKNOWN_MESSAGE = new LazyJVM<>(() -> SupernovaMessage::unknown);
	public static final Lazy<MiraiLogger> LOGGER = new LazyJVM<>(() -> MiraiLogger.Factory.INSTANCE.create(SupernovaMessage.class));
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA = "data";
	public static final String KEY_TEXT = "text";
	public static final String KEY_FILE = "file";
	public static final String KEY_ID = "id";
	public static final String KEY_SUMMARY = "summary";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_FACE = "face";
	public static final String TYPE_JSON = "json";
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
			Function<Json, SingleMessage> deserializer = DESERIALIZERS.getOrDefault(type, UNKNOWN_MESSAGE.getValue());
			builder.add(deserializer.apply(val));
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

	public static PlainText text(Json val)
	{
		Json data = val.get(KEY_DATA);
		return new PlainText(data.string(KEY_TEXT));
	}

	public static SupernovaImage image(Json val)
	{
		Json data = val.get(KEY_DATA);
		SupernovaImage img = new SupernovaImage(data.string(KEY_FILE));
		img.raw = data.stringify();
		return img;
	}

	public static Face face(Json val)
	{
		Json data = val.get(KEY_DATA);
		return new Face(Integer.parseInt(data.string(KEY_ID)));
	}

	public static LightApp app(Json val)
	{
		Json data = val.get(KEY_DATA);
		return new LightApp(data.string(KEY_DATA));
	}

	public static UnknownMessage unknown(Json val)
	{
		return new UnknownMessage(val);
	}

	public static void register(String type, Function<Json, SingleMessage> deserializer)
	{
		DESERIALIZERS.put(type, deserializer);
	}

	static
	{
		register(TYPE_TEXT, SupernovaMessage::text);
		register(TYPE_IMAGE, SupernovaMessage::image);
		register(TYPE_FACE, SupernovaMessage::face);
		register(TYPE_JSON, SupernovaMessage::app);
	}
}
