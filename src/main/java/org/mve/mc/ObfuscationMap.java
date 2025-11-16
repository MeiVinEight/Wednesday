package org.mve.mc;

import org.mve.ModuleAccess;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ObfuscationMap
{
	public static final int TYPE_OBFUSCATE = 0;
	public static final int TYPE_SEARGE    = 1;
	public static final int TYPE_OFFICIAL  = 2;
	public final Map<String, TypeName> map;
	public final int field;
	public final int method;

	public ObfuscationMap(String version)
	{
		InputStream iobf2srg = ObfuscationMap.class.getResourceAsStream("/mc/" + version + "/obf_to_srg.tsrg2");
		InputStream isrg2mcp = ObfuscationMap.class.getResourceAsStream("/mc/" + version + "/srg_to_official_1.20.1.tsrg");
		InputStream isuprmap = ObfuscationMap.class.getResourceAsStream("/mc/" + version + "/super_map.txt");
		InputStream ifldtype = ObfuscationMap.class.getResourceAsStream("/mc/" + version + "/field_type.txt");
		Map<String, TypeName> mapTyp = new HashMap<>();
		int countField = 0;
		int countMethod = 0;
		try (iobf2srg; isrg2mcp; isuprmap; ifldtype)
		{
			if (iobf2srg == null)
				throw new FileNotFoundException("/mc/" + version + "/obf_to_srg.tsrg2");
			if (isrg2mcp == null)
				throw new FileNotFoundException("/mc/" + version + "/srg_to_official_1.20.1.tsrg");
			if (isuprmap == null)
				throw new FileNotFoundException("/mc/" + version + "/super_map.txt");
			if (ifldtype == null)
				throw new FileNotFoundException("/mc/" + version + "/field_type.txt");

			Scanner obf2srg = new Scanner(iobf2srg);
			Scanner srg2mcp = new Scanner(isrg2mcp);
			Scanner suprmap = new Scanner(isuprmap);
			Scanner fldtype = new Scanner(ifldtype);
			obf2srg.nextLine();
			srg2mcp.nextLine();


			Map<String, Map<String, MemberName>> mapMbr = new HashMap<>();
			Map<String, MemberName> currMbr = null;
			TypeName currType = null;
			while (obf2srg.hasNextLine())
			{
				String line = obf2srg.nextLine();
				if (line.charAt(1) == '\t')
					continue;
				if (line.charAt(0) == '\t')
				{
					String[] args = line.substring(1).split(" ");
					String obfName = args[0];
					String srgName = args[args.length == 2 ? 1 : 2];
					if (args.length == 2)
						countField++;
					else
						countMethod++;
					MemberName mn = new MemberName();
					mn.declare = currType;
					mn.obfuscate = obfName;
					mn.searge = srgName;
					assert currMbr != null;
					currMbr.put(srgName, mn);
					continue;
				}

				String[] obf_srg = line.split(" ");
				String obfName = obf_srg[0];
				String srgName = obf_srg[1];
				TypeName type = currType = new TypeName();
				type.obfuscate = obfName;
				type.searge = srgName;
				mapTyp.put(srgName, type);
				mapMbr.put(srgName, currMbr = new HashMap<>());
			}

			while (srg2mcp.hasNextLine())
			{
				String line = srg2mcp.nextLine();
				if (line.charAt(1) == '\t')
					continue;
				if (line.charAt(0) == '\t')
				{
					String[] args = line.substring(1).split(" ");
					String srgName = args[0];
					String offName = args[args.length == 2 ? 1 : 2];
					String desc = (args.length == 2) ? null : args[1];
					assert currMbr != null;
					MemberName mbr = currMbr.get(srgName);
					if (mbr == null)
						continue;
					mbr.official = offName;
					mbr.type = desc;
					continue;
				}

				String className = line.split(" ")[0];
				currMbr = mapMbr.getOrDefault(className, new HashMap<>());
			}

			while (suprmap.hasNextLine())
			{
				String line = suprmap.nextLine();
				String[] supers = line.split(" ");
				String className = supers[0];
				String superName = (supers.length == 2) ? supers[1] : null;
				TypeName type = mapTyp.get(className);
				if (type == null)
					continue;
				type.superclass = mapTyp.get(superName);
			}

			while (fldtype.hasNextLine())
			{
				String line = fldtype.nextLine();
				if (line.charAt(0) != '\t')
				{
					currMbr = mapMbr.getOrDefault(line, new HashMap<>());
					continue;
				}
				String[] args = line.substring(1).split(" ");
				String srgName = args[0];
				String desc = args[1];
				assert currMbr != null;
				if (currMbr.containsKey(srgName))
					currMbr.get(srgName).type = desc;
			}

			mapMbr.forEach((k, v) ->
			{
				TypeName type = mapTyp.get(k);
				if (type == null)
					return;
				type.member = mapMbr.getOrDefault(type.searge, new HashMap<>()).values().toArray(new MemberName[0]);
			});
		}
		catch (Throwable e)
		{
			ModuleAccess.exception(e);
		}
		this.field = countField;
		this.method = countMethod;
		map = mapTyp;
	}

	public TypeName[] search(String typeName)
	{
		return this.map.values().stream().filter(typ -> Objects.equals(typeName, typ.obfuscate)).toArray(TypeName[]::new);
	}

	public List<MemberName> search(String memberName, int nameType)
	{
		List<MemberName> retVal = new ArrayList<>();
		if (memberName == null)
			return retVal;
		Map<String, MemberName> srgs = new HashMap<>();
		switch (nameType)
		{
			case TYPE_OBFUSCATE:
			{
				this.map.forEach((srgName, typ) ->
				{
					for (MemberName member : typ.member)
					{
						if (!Objects.equals(memberName, member.obfuscate))
							continue;
						MemberName mn = ObfuscationMap.override(member);
						srgs.putIfAbsent(mn.searge, mn);
					}
				});
				break;
			}
			case TYPE_SEARGE:
			{
				this.map.forEach((srgName, typ) ->
				{
					for (MemberName member : typ.member)
					{
						if (!Objects.equals(memberName, member.searge))
							continue;
						MemberName mn = ObfuscationMap.override(member);
						srgs.putIfAbsent(mn.searge, mn);
					}
				});
				break;
			}
			case TYPE_OFFICIAL:
			{
				this.map.forEach((srgName, typ) ->
				{
					for (MemberName mbr : typ.member)
					{
						if (!Objects.equals(memberName, mbr.official))
							continue;
						MemberName mn = ObfuscationMap.override(mbr);
						srgs.putIfAbsent(mn.searge, mn);
					}
				});
				break;
			}
		}
		retVal.addAll(srgs.values());
		return retVal;
	}

	public List<MemberName> search(String className, String memberName, int nameType)
	{
		List<MemberName> retVal = new ArrayList<>();
		if (className == null || memberName == null)
			return retVal;
		switch (nameType)
		{
			case TYPE_OBFUSCATE:
			{
				this.map.forEach((srgName, typ) ->
				{
					if (!Objects.equals(className, typ.obfuscate))
						return;
					for (MemberName member : typ.member)
						if (Objects.equals(memberName, member.obfuscate))
							retVal.add(member);
				});
				break;
			}
			case TYPE_SEARGE:
			{
				TypeName type = this.map.get(className);
				if (type == null)
					break;
				for (MemberName member : type.member)
					if (Objects.equals(memberName, member.searge))
						retVal.add(member);
				break;
			}
			case TYPE_OFFICIAL:
			{
				TypeName type = this.map.get(className);
				if (type == null)
					break;
				for (MemberName member : type.member)
					if (Objects.equals(memberName, member.official))
						retVal.add(member);
				break;
			}
		}
		return retVal;
	}

	public static MemberName override(MemberName member)
	{
		TypeName declare = member.declare;
		while (declare.superclass != null)
		{
			declare = declare.superclass;
			for (MemberName superMbr : declare.member)
				if (Objects.equals(superMbr.searge, member.searge))
					member = superMbr;
		}
		return member;
	}
}
