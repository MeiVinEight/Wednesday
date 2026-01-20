package org.mve.uni;

import net.mamoe.mirai.message.data.ShortVideo;
import net.mamoe.mirai.utils.ExternalResource;
import org.mve.Configuration;
import org.mve.Mirroring;
import top.mrxiaom.overflow.internal.message.OnebotMessages;
import top.mrxiaom.overflow.internal.message.data.WrappedVideo;
import top.mrxiaom.overflow.internal.utils.ResourceUtilsKt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ResourceManager
{
	public static ShortVideo video(String relativePath, String fileName, String fileId)
	{
		if (fileName == null)
			fileName = new File(relativePath).getName();
		if (fileId == null)
			fileId = fileName;
		String url = Configuration.FILE_SERVER + '/' + relativePath;
		ShortVideo video = new WrappedVideo(url, fileName, fileId);
		if (url.startsWith("file:///"))
		{
			File file = new File(url.substring(8));
			if (!file.isFile())
			{
				Mirroring.thrown(new FileNotFoundException(file.getPath()));
				throw new RuntimeException();
			}
			try (ExternalResource resource = ExternalResource.create(file))
			{
				video = OnebotMessages.INSTANCE.videoFromFile$overflow_core(ResourceUtilsKt.toBase64File(resource));
			}
			catch (IOException e)
			{
				Mirroring.thrown(e);
			}
		}
		return video;
	}
}
