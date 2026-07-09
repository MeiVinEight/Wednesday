package org.mve.mixin;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class EmptyFieldVisitor extends FieldVisitor
{
	public EmptyFieldVisitor()
	{
		super(Opcodes.ASM9);
	}
}
