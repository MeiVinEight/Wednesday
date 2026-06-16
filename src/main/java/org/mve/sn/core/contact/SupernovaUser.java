package org.mve.sn.core.contact;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.User;
import org.jetbrains.annotations.NotNull;
import org.mve.sn.SupernovaAPI;
import org.mve.sn.core.APIResponse;
import org.mve.sn.core.Supernova;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public abstract class SupernovaUser extends SupernovaContact implements User
{
	public final LazyJVM<String> nickname;
	public final LazyJVM<String> remark;

	public SupernovaUser(Bot context, long id)
	{
		super(context, id);
		this.nickname = new LazyJVM<>(() -> {
			APIResponse response = SupernovaAPI.getStrangerInfo((Supernova) (this.getBot()), this.getId(), true);
			response.checkValidation();
			Json data = response.data;
			return data.string(SupernovaAPI.KEY_NICKNAME);
		});
		this.remark = new LazyJVM<>(() -> "");
	}

	@NotNull
	@Override
	public String getRemark()
	{
		return this.remark.getValue();
	}

	@NotNull
	@Override
	public String getNick()
	{
		return this.nickname.getValue();
	}
}
