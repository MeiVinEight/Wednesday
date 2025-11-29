package org.mve.mc;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.mve.LoggerMessage;
import org.mve.Wednesday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Minecraft
{
	private static final Pattern NAME_AND_TYPE = Pattern.compile("^([\\w$/]+)#([\\w$]+)$");
	private static final Pattern TYPE_ONLY = Pattern.compile("^([\\w$/]+)#$");
	private static final Pattern NAME_ONLY = Pattern.compile("^[\\w$]+$");
	private static final String VERSION = "1.20.1";
	private static final Map<String, ObfuscationMap> OBFUSCATIONS = new HashMap<>();

	public Boolean official(MessageEvent event)
	{
		return Minecraft.subject(event, ObfuscationMap.TYPE_OFFICIAL);
	}

	public Boolean searge(MessageEvent event)
	{
		return Minecraft.subject(event, ObfuscationMap.TYPE_SEARGE);
	}

	public Boolean obfuscate(MessageEvent event)
	{
		return Minecraft.subject(event, ObfuscationMap.TYPE_OBFUSCATE);
	}

	public static Boolean subject(MessageEvent event, int type)
	{
		MessageChain msg = event.getMessage();
		if (msg.size() <= 1)
			return false;
		if (!(msg.get(1) instanceof PlainText text))
			return false;
		if (OBFUSCATIONS.get(VERSION) == null)
			return false;
		ObfuscationMap obf = OBFUSCATIONS.get(VERSION);
		String name = text.getContent().substring(4).trim();
		subject(obf, event.getSubject(), name, type);
		return true;
	}

	private static void subject(ObfuscationMap map, Contact subject, String name, int type)
	{
		if (name.isEmpty())
			return;
		Matcher matcher = NAME_AND_TYPE.matcher(name);
		if (matcher.matches())
		{
			String className = matcher.group(1);
			String typeName = matcher.group(2);
			List<MemberName> members = map.search(className, typeName, type);
			subject(map, subject, name, members);
			return;
		}

		matcher = NAME_ONLY.matcher(name);
		if (matcher.matches())
		{
			List<MemberName> members = map.search(name, type);
			subject(map, subject, name, members);
			return;
		}

		matcher = TYPE_ONLY.matcher(name);
		if (matcher.matches())
		{
			String typeName = matcher.group(1);
			if (type == ObfuscationMap.TYPE_OBFUSCATE)
			{
				TypeName[] tns = map.search(typeName);
				if (tns.length == 0)
				{
					subject.sendMessage("Version: " + VERSION + '\n' + typeName + " - NOT FOUND");
					return;
				}
				if (tns.length == 1)
				{
					TypeName tn = tns[0];
					subject.sendMessage("Version: " + VERSION + "\nName: " + tn.obfuscate + " => " + tn.searge);
					return;
				}
				StringBuilder builder = new StringBuilder("Version: ")
					.append(VERSION)
					.append('\n')
					.append(typeName)
					.append(" - NOT UNIQUE");
				for (TypeName tn : tns)
					builder.append('\n').append(tn.searge);
				subject.sendMessage(builder.toString());
			}
			else if (type == ObfuscationMap.TYPE_SEARGE || type == ObfuscationMap.TYPE_OFFICIAL)
			{
				TypeName tn = map.map.get(typeName);
				if (tn == null)
					subject.sendMessage("Version: " + VERSION + '\n' + typeName + " - NOT FOUND");
				else
					subject.sendMessage("Version: " + VERSION + "\nName: " + tn.obfuscate + " => " + tn.searge);
			}
		}
	}

	private static void subject(ObfuscationMap map, Contact subject, String mcpName, List<MemberName> members)
	{
		if (members.isEmpty())
		{
			subject.sendMessage("Version: " + VERSION + '\n' + mcpName + " - NOT FOUND");
			return;
		}
		if (members.size() == 1)
		{
			subject(map, subject, members.get(0));
			return;
		}
		StringBuilder builder = new StringBuilder("Version: ")
			.append(VERSION)
			.append('\n')
			.append(mcpName)
			.append(" - NOT UNIQUE");
		for (MemberName member : members)
			builder.append('\n')
				.append(member.declare.searge)
				.append(".")
				.append(member.searge)
				.append(member.type);
		subject.sendMessage(builder.toString());
	}

	private static void subject(ObfuscationMap map, Contact subject, MemberName member)
	{
		StringBuilder builder = new StringBuilder("Version: ")
			.append(VERSION)
			.append("\nClass: ")
			.append(member.declare.obfuscate)
			.append(" => ")
			.append(member.declare.searge)
			.append("\nName: ")
			.append(member.obfuscate)
			.append(" => ")
			.append(member.searge)
			.append(" => ")
			.append(member.official)
			.append("\nType: ");
		if (member.type == null)
			builder.append("None");
		else
		{
			boolean param = false;
			boolean ltype = false;
			StringBuilder paramBuilder = new StringBuilder();
			ArrayList<String> paramType = null;
			StringBuilder returnType = new StringBuilder();
			for (char ch : member.type.toCharArray())
			{
				if (ch == '(')
				{
					param = true;
					paramType = new ArrayList<>();
					continue;
				}
				if (ch == ')')
				{
					param = false;
					continue;
				}
				if (ch == 'L' && !ltype)
				{
					ltype = true;
					paramBuilder = new StringBuilder();
					if (param)
						paramBuilder = new StringBuilder().append(ch);
					else
						returnType.append(ch);
					continue;
				}
				if (ch == ';')
				{
					ltype = false;
					if (param)
						paramType.add(paramBuilder.append(ch).toString());
					else
						returnType.append(ch);
					continue;
				}
				if (ltype)
				{
					if (param)
						paramBuilder.append(ch);
					else
						returnType.append(ch);
				}
				else
					if (param)
						paramType.add(String.valueOf(ch));
					else
						returnType.append(ch);
			}

			StringBuilder obfMethodType = new StringBuilder();
			if (paramType != null)
			{
				obfMethodType.append('(');
				for (String paramName : paramType)
					obfMethodType.append(obfuscateType(map, paramName));
				obfMethodType.append(')');
			}
			obfMethodType.append(obfuscateType(map, returnType.toString()));

			builder.append(obfMethodType)
				.append(" => ")
				.append(member.type)
				.append("\nAT: public ")
				.append(member.declare.searge.replace('/', '.'))
				.append(' ')
				.append(member.searge)
				.append(member.type)
				.append(" # ")
				.append(member.official);
		}
		subject.sendMessage(builder.toString());
	}

	private static String obfuscateType(ObfuscationMap map, String typeName)
	{
		if (typeName.charAt(0) == 'L')
		{
			String type = typeName.substring(1, typeName.length() - 1);
			if (map.map.get(type) != null)
				return 'L' + map.map.get(type).obfuscate + ';';
		}
		return typeName;
	}

	static
	{
		ObfuscationMap map;
		try
		{
			Wednesday.LOGGER.info(LoggerMessage.translate("log.mc.load.obf.map"), VERSION);
			map = new ObfuscationMap(VERSION);
			OBFUSCATIONS.put(VERSION, map);
			Wednesday.LOGGER.info(LoggerMessage.translate("log.mc.load.obf.map.succ"), VERSION, map.map.size(), map.field + map.method, map.field, map.method);
		}
		catch (Throwable e)
		{
			Wednesday.LOGGER.error(LoggerMessage.translate("log.mc.load.obf.map.fail"), VERSION, e);
		}
	}
}
