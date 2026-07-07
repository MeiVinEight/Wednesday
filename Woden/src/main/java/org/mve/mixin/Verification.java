package org.mve.mixin;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Stack;

public class Verification
{
	public final Stack<String> stack = new Stack<>();
	public final String clazz;
	public final String method;

	public Verification(String clazz, String method)
	{
		this.clazz = clazz;
		this.method = method;
	}

	public boolean visit(AbstractInsnNode insn)
	{
		if (insn instanceof VarInsnNode)
		{
		}
		return true;
	}
}
