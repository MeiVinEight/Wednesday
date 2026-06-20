package org.mve.mixin;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Map;

public class MixinMethodVisitor extends MethodVisitor
{
	public final int access;
	public final String name;
	public final String descriptor;
	public final String signature;
	public final String[] exceptions;
	public final MixinInjection[] injection;
	public final Map<Object, Object> mapping;
	public int[] variable = new int[0];

	public MixinMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions, MixinInjection[] injection, Map<Object, Object> mapping)
	{
		super(Opcodes.ASM9, methodVisitor);
		this.access = access;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
		this.injection = injection;
		this.mapping = mapping;
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
		this.variable[varIndex] = switch (opcode)
		{
			case Opcodes.ILOAD -> Type.INT;
			case Opcodes.FLOAD -> Type.FLOAT;
			case Opcodes.DLOAD -> Type.DOUBLE;
			case Opcodes.LLOAD -> Type.LONG;
			case Opcodes.ALOAD -> Type.OBJECT;
			default -> Type.VOID;
		};
		switch (opcode)
		{
			case Opcodes.ISTORE:
			case Opcodes.FSTORE:
			case Opcodes.DSTORE:
			case Opcodes.ASTORE:
				this.expandVarTable(varIndex + 1);
				break;
			case Opcodes.LSTORE:
				this.expandVarTable(varIndex + 2);
				break;
		}
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String descriptor)
	{
		MethodRef ref = new MethodRef(owner, name, descriptor);
		ref = (MethodRef) this.mapping.getOrDefault(ref, ref);
		owner = ref.clazz;
		name = ref.name;
		descriptor = ref.type;

		for (MixinInjection inj : this.injection)
			inj.visit(this, new FieldInsnNode(opcode, owner, name, descriptor), Inject.SHIFT_BEFORE);

		super.visitFieldInsn(opcode, owner, name, descriptor);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new FieldInsnNode(opcode, owner, name, descriptor), Inject.SHIFT_AFTER);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface)
	{
		MethodRef ref = new MethodRef(owner, name, descriptor);
		ref = (MethodRef) this.mapping.getOrDefault(ref, ref);
		owner = ref.clazz;
		name = ref.name;
		descriptor = ref.type;

		for (MixinInjection inj : this.injection)
			inj.visit(this, new MethodInsnNode(opcode, owner, name, descriptor, isInterface), Inject.SHIFT_BEFORE);

		super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new MethodInsnNode(opcode, owner, name, descriptor, isInterface), Inject.SHIFT_AFTER);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String descriptor, Handle boot, Object... args)
	{
		if ("java/lang/invoke/LambdaMetafactory".equals(boot.getOwner()) &&
			"metafactory".equals(boot.getName()) &&
			("(Ljava/lang/invoke/MethodHandles$Lookup;" +
				"Ljava/lang/String;" +
				"Ljava/lang/invoke/MethodType;" +
				"Ljava/lang/invoke/MethodType;" +
				"Ljava/lang/invoke/MethodHandle;" +
				"Ljava/lang/invoke/MethodType;" +
				")Ljava/lang/invoke/CallSite;").equals(boot.getDesc()))
		{
			Handle handle = (Handle) args[1];
			MethodRef ref = new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc());
			ref = (MethodRef) this.mapping.getOrDefault(ref, ref);
			args[1] = new Handle(handle.getTag(), ref.clazz, ref.name, ref.type, handle.isInterface());
		}

		for (MixinInjection inj : this.injection)
			inj.visit(this, new InvokeDynamicInsnNode(name, descriptor, boot, args), Inject.SHIFT_BEFORE);

		super.visitInvokeDynamicInsn(name, descriptor, boot, args);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new InvokeDynamicInsnNode(name, descriptor, boot, args), Inject.SHIFT_AFTER);
	}

	public MethodVisitor mv()
	{
		return this.mv;
	}

	public void expandVarTable(int length)
	{
		if (this.variable.length >= length)
			return;
		int[] oldTable = this.variable;
		int oldLen = this.variable.length;
		this.variable = new int[length];
		System.arraycopy(oldTable, 0, this.variable, 0, oldLen);
	}

	public int varType(int idx)
	{
		if (this.variable.length <= idx)
			return Type.VOID;
		return this.variable[idx];
	}
}
