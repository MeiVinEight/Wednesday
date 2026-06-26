package org.mve.sn.message;

import kotlin.Lazy;
import net.mamoe.mirai.message.data.AudioCodec;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.message.data.OnlineAudio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;
import org.mve.uni.MD5;

import java.security.MessageDigest;

public class WrappedAudio implements OfflineAudio, OnlineAudio, MessageJson
{
	public final Supernova context;
	public final String file;
	public final String url;
	public final Lazy<byte[]> digest;
	public final Lazy<AudioCodec> codec;
	public String message;

	public WrappedAudio(Supernova context, String file, String url)
	{
		this.context = context;
		this.file = file;
		this.url = url;
		this.digest = new LazyJVM<>(() ->
		{
			MessageDigest md5 = MD5.get();
			return md5.digest();
		});
		this.codec = new LazyJVM<>(() ->
		{
			if (".amr".equals(this.file.substring(this.file.lastIndexOf('.'))))
				return AudioCodec.AMR;
			return AudioCodec.SILK;
		});
	}

	@Override
	public long getLength()
	{
		return 0;
	}

	@NotNull
	@Override
	public String getUrlForDownload()
	{
		return this.url;
	}

	@NotNull
	@Override
	public String getFilename()
	{
		return this.file;
	}

	@NotNull
	@Override
	public byte[] getFileMd5()
	{
		return this.digest.getValue();
	}

	@Override
	public long getFileSize()
	{
		return 0;
	}

	@NotNull
	@Override
	public AudioCodec getCodec()
	{
		return this.codec.getValue();
	}

	@Nullable
	@Override
	public byte[] getExtraData()
	{
		return new byte[0];
	}

	@NotNull
	@Override
	public String contentToString()
	{
		return "[语音]";
	}

	@Override
	public Json json()
	{
		if (this.message != null)
			return Json.resolve(this.message);
		return new Json()
			.set(SupernovaMessage.KEY_TYPE, SupernovaMessage.TYPE_RECORD)
			.set(SupernovaMessage.KEY_DATA, new Json()
				.set(SupernovaMessage.KEY_FILE, this.file)
				.set(SupernovaMessage.KEY_URL, this.url)
			);
	}
}
