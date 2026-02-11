package org.mve.mixin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

public class MixinMethodVisitor extends MethodVisitor
{
	public final int access;
	public final String name;
	public final String descriptor;
	public final String signature;
	public final String[] exceptions;
	public final MixinInjection[] injection;
	public int[] varialeTable = new int[0];
	private int stack = 0;

	public MixinMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions, MixinInjection[] injection)
	{
		super(Opcodes.ASM9, methodVisitor);
		this.access = access;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
		this.injection = injection;
		int locals = Type.getArgumentCount(descriptor);
		if ((access & Opcodes.ACC_STATIC) == 0)
			locals++;
		this.expandVarTable(locals);
	}

	@Override
	public void visitCode()
	{
		super.visitCode();
		for (MixinInjection inj : this.injection)
			inj.visit(this, null, Inject.SHIFT_BEFORE);
	}

	@Override
	public void visitVarInsn(int opcode, int varIndex)
	{
		super.visitVarInsn(opcode, varIndex);
		this.expandVarTable(varIndex + 1);
		this.varialeTable[varIndex] = switch (opcode)
		{
			case Opcodes.ILOAD -> Type.INT;
			case Opcodes.FLOAD -> Type.FLOAT;
			case Opcodes.DLOAD -> Type.DOUBLE;
			case Opcodes.LLOAD -> Type.LONG;
			case Opcodes.ALOAD -> Type.OBJECT;
			default -> Type.VOID;
		};
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface)
	{
		for (MixinInjection inj : this.injection)
			inj.visit(this, new MethodInsnNode(opcode, owner, name, descriptor, isInterface), Inject.SHIFT_BEFORE);

		super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new MethodInsnNode(opcode, owner, name, descriptor, isInterface), Inject.SHIFT_AFTER);
	}

	public MethodVisitor mv()
	{
		return this.mv;
	}

	public void expandVarTable(int length)
	{
		if (this.varialeTable.length >= length)
			return;
		int[] oldTable = this.varialeTable;
		int oldLen = this.varialeTable.length;
		this.varialeTable = new int[length];
		System.arraycopy(oldTable, 0, this.varialeTable, 0, oldLen);
	}

	public int varType(int idx)
	{
		if (this.varialeTable.length <= idx)
			return Type.VOID;
		return this.varialeTable[idx];
	}
}
