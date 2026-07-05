package org.mve.orange.core.contact;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.User;
import org.jetbrains.annotations.NotNull;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.APIResponse;
import org.mve.orange.core.Orange;
import org.mve.uni.Json;
import org.mve.uni.LazyJVM;

public abstract class UserX extends ContactX implements User
{
	public final LazyJVM<String> nickname;
	public final LazyJVM<String> remark;

	public UserX(Bot context, long id)
	{
		super(context, id);
		this.nickname = new LazyJVM<>(() -> {
			APIResponse response = OrangeAPI.getStrangerInfo((Orange) (this.getBot()), this.getId(), true);
			response.checkValidation();
			Json data = response.data;
			return data.string(OrangeAPI.KEY_NICKNAME);
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
