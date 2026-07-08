package org.mve.web.service;

import org.mve.uni.Json;
import org.mve.web.WebAPI;
import org.mve.web.WebService;
import org.mve.web.WednesdayWeb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class WebSystemInfo implements WebService
{
	@Override
	public Object service(Json body)
	{
		String osName = System.getProperty("os.name");
		String ver = System.getProperty("os.version");
		try
		{
			if (osName.startsWith("Windows"))
			{
				Process proc = Runtime.getRuntime().exec("cmd /c ver");
				proc.onExit().join();
				Scanner sc = new Scanner(proc.getInputStream());
				while (sc.hasNextLine())
				{
					String line = sc.nextLine();
					if (line.isEmpty())
						continue;

					// Microsoft Windows [Version
					ver = line.substring(27, line.length() - 1);
					break;
				}
			}
			if (osName.startsWith("Linux"))
			{
				Process proc = Runtime.getRuntime().exec("sh -c \"uname -r\"");
				proc.onExit().join();
				try (BufferedReader reader = proc.inputReader())
				{
					ver = reader.readLine();
				}
			}
		}
		catch (IOException e)
		{
			WednesdayWeb.LOGGER.error("获取系统版本信息失败", e);
		}
		return WebAPI.code(new Json(), WebAPI.CODE_OK)
			.set(WebAPI.KEY_DATA, new Json()
				.set(WebAPI.KEY_SYSTEM, new Json()
					.set(WebAPI.KEY_TYPE, System.getProperty("os.name"))
					.set(WebAPI.KEY_VERSION, ver)
				)
			);
	}
}
