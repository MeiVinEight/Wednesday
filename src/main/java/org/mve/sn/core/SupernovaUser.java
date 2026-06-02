package org.mve.sn.core;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.User;

public abstract class SupernovaUser extends SupernovaContact implements User
{
	public SupernovaUser(Bot context, long id)
	{
		super(context, id);
	}
}
