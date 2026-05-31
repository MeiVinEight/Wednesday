package org.mve.uni;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

public class MemoryManagement
{
	public final List<MemoryPoolMXBean> managing;
	public final List<StackingMemory> stacking;

	private MemoryManagement(List<MemoryPoolMXBean> managing, List<StackingMemory> stacking)
	{
		this.managing = List.copyOf(managing);
		this.stacking = List.copyOf(stacking);
	}

	public static MemoryManagement capture()
	{
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		return new MemoryManagement(pools, List.of());
	}

	public static native StackingMemory stacking(Thread thread);
}
