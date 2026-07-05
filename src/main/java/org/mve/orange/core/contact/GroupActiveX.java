package org.mve.orange.core.contact;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import net.mamoe.mirai.contact.active.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mve.orange.core.Orange;

import java.util.List;
import java.util.Map;

public class GroupActiveX implements GroupActive
{
	private final Orange context;
	private final long group;

	public GroupActiveX(Orange context, long group)
	{
		this.context = context;
		this.group = group;
	}

	@Nullable
	@Override
	public Object queryHonorHistory(int i, @NotNull Continuation<? super ActiveHonorList> continuation)
	{
		return null;
	}

	@Override
	public boolean isHonorVisible()
	{
		return false;
	}

	@Nullable
	@Override
	public Object setHonorVisible(boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Override
	public boolean isTitleVisible()
	{
		return false;
	}

	@Nullable
	@Override
	public Object setTitleVisible(boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Override
	public boolean isTemperatureVisible()
	{
		return false;
	}

	@Nullable
	@Override
	public Object setTemperatureVisible(boolean b, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Map<Integer, String> getRankTitles()
	{
		return Map.of();
	}

	@Nullable
	@Override
	public Object setRankTitles(@NotNull Map<Integer, String> map, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Map<Integer, String> getTemperatureTitles()
	{
		return Map.of();
	}

	@Nullable
	@Override
	public Object setTemperatureTitles(@NotNull Map<Integer, String> map, @NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object refresh(@NotNull Continuation<? super Unit> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object queryChart(@NotNull Continuation<? super ActiveChart> continuation)
	{
		return null;
	}

	@Nullable
	@Override
	public Object queryActiveRank(@NotNull Continuation<? super List<ActiveRankRecord>> continuation)
	{
		return null;
	}

	@NotNull
	@Override
	public Flow<ActiveRecord> asFlow()
	{
		return null;
	}
}
