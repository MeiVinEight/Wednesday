package org.mve.sn.message;

import kotlin.Lazy;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Dice;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.PokeMessage;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.RockPaperScissors;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.Supernova;
import org.mve.sn.data.SourceOffline;
import org.mve.sn.message.app.ILightApp;
import org.mve.sn.message.app.MiniApp;
import org.mve.sn.message.app.UnknownApp;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SupernovaMessage implements Message, SingleMessage
{
	private static final Map<String, BiFunction<Supernova, Json, SingleMessage>> DESERIALIZERS = new HashMap<>();
	private static final Map<String, Function<String, ILightApp>> LIGHT_APP = new HashMap<>();
	private static final Lazy<BiFunction<Supernova, Json, SingleMessage>> UNKNOWN_MESSAGE = new LazyJVM<>(() -> SupernovaMessage::unknown);
	public static final Lazy<MiraiLogger> LOGGER = new LazyJVM<>(() -> MiraiLogger.Factory.INSTANCE.create(SupernovaMessage.class));
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA = "data";
	public static final String KEY_TEXT = "text";
	public static final String KEY_FILE = "file";
	public static final String KEY_ID = "id";
	public static final String KEY_SUMMARY = "summary";
	public static final String KEY_QQ = "qq";
	public static final String KEY_URL = "url";
	public static final String KEY_RESULT = "result";
	public static final String KEY_FILE_ID = "file_id";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_FACE = "face";
	public static final String TYPE_JSON = "json";
	public static final String TYPE_AT = "at";
	public static final String TYPE_REPLY = "reply";
	public static final String TYPE_RECORD = "record";
	public static final String TYPE_DICE = "dice";
	public static final String TYPE_RPS = "rps";
	public static final String TYPE_POKE = "poke";
	public static final String TYPE_FILE = "file";
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
			BiFunction<Supernova, Json, SingleMessage> deserializer = DESERIALIZERS.getOrDefault(type, UNKNOWN_MESSAGE.getValue());
			builder.add(deserializer.apply(this.context, val));
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

	public static PlainText text(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		return new PlainText(data.string(KEY_TEXT));
	}

	public static WrappedImage image(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		WrappedImage img = new WrappedImage(data.string(KEY_FILE));
		img.raw = data.stringify();
		return img;
	}

	public static Face face(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		return new Face(Integer.parseInt(data.string(KEY_ID)));
	}

	public static LightApp app(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		return new LightApp(data.string(KEY_DATA));
	}

	public static SingleMessage at(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		String qq = data.string(SupernovaMessage.KEY_QQ);
		if ("all".equals(qq))
			return AtAll.INSTANCE;
		return new At(Long.parseLong(qq));
	}

	public static QuoteReply reply(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		int id = Integer.parseInt(data.string(SupernovaMessage.KEY_ID));
		MessageSource source = context.source(id);
		if (source == null)
			source = new SourceOffline(context, id);
		return new QuoteReply(source);
	}

	public static WrappedAudio record(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		String file = data.string(SupernovaMessage.KEY_FILE);
		String url = data.string(SupernovaMessage.KEY_URL);
		WrappedAudio audio = new WrappedAudio(context, file, url);
		audio.message = val.stringify();
		return audio;
	}

	public static Dice dice(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		int result = Integer.parseInt(data.string(SupernovaMessage.KEY_RESULT));
		return new Dice(result);
	}

	public static RockPaperScissors rps(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		int result = Integer.parseInt(data.string(SupernovaMessage.KEY_RESULT));
		return RockPaperScissors.values()[result - 1];
	}

	public static PokeMessage poke(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		int id = Integer.parseInt(data.string(SupernovaMessage.KEY_ID));
		int type = Integer.parseInt(data.string(SupernovaMessage.KEY_TYPE));
		return new PokeMessage("", type, id);
	}

	public static FileMessage file(Supernova context, Json val)
	{
		Json data = val.get(KEY_DATA);
		String file = data.string(SupernovaMessage.KEY_FILE);
		String fileId = data.string(SupernovaMessage.KEY_FILE_ID);
		String url = data.string(SupernovaMessage.KEY_URL);
		return new WrappedFileMessage(fileId, file, url);
	}

	public static UnknownMessage unknown(Supernova context, Json val)
	{
		return new UnknownMessage(val);
	}

	public static ILightApp resolveApp(String type, String content)
	{
		return LIGHT_APP.getOrDefault(type, UnknownApp::new).apply(content);
	}

	public static void registerMessage(String type, BiFunction<Supernova, Json, SingleMessage> deserializer)
	{
		DESERIALIZERS.put(type, deserializer);
	}

	public static void registerApp(String type, Function<String, ILightApp> app)
	{
		LIGHT_APP.put(type, app);
	}

	static
	{
		registerMessage(TYPE_TEXT, SupernovaMessage::text);
		registerMessage(TYPE_IMAGE, SupernovaMessage::image);
		registerMessage(TYPE_FACE, SupernovaMessage::face);
		registerMessage(TYPE_JSON, SupernovaMessage::app);
		registerMessage(TYPE_AT, SupernovaMessage::at);
		registerMessage(TYPE_REPLY, SupernovaMessage::reply);
		registerMessage(TYPE_RECORD, SupernovaMessage::record);
		registerMessage(TYPE_DICE, SupernovaMessage::dice);
		registerMessage(TYPE_RPS, SupernovaMessage::rps);
		registerMessage(TYPE_POKE, SupernovaMessage::poke);
		registerMessage(TYPE_FILE, SupernovaMessage::file);
		registerApp(ILightApp.APP_MINIAPP01, MiniApp::resolve);
	}
}
