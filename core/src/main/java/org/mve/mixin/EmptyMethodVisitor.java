package org.mve.mixin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class EmptyMethodVisitor extends MethodVisitor
{
	public EmptyMethodVisitor()
	{
		super(Opcodes.ASM9);
	}
}
