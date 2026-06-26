package org.mve.mixin;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Map;
import java.util.stream.Stream;

public class MixinMethodVisitor extends MethodVisitor
{
	public final MixinClassVisitor clazz;
	public final int access;
	public final String name;
	public final String descriptor;
	public final String signature;
	public final String[] exceptions;
	public final MixinInjection[] injection;
	public final Map<Object, Object> mapping;
	public final int lineNumber;
	public int[] variable = new int[0];

	public MixinMethodVisitor(MixinClassVisitor clazz, MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions)
	{
		super(Opcodes.ASM9, methodVisitor);
		this.clazz = clazz;
		this.access = access;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
		this.injection = Stream.of(clazz.mixin)
			.map(mi -> mi.injection(name, descriptor))
			.flatMap(Stream::of)
			.peek(mi -> mi.preapply(clazz))
			.toArray(MixinInjection[]::new);
		this.mapping = clazz.mapping;
		this.lineNumber = clazz.lineNumber;
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
		for (MixinInjection inj : this.injection)
			inj.visit(this, new InvokeDynamicInsnNode(name, descriptor, boot, args), Inject.SHIFT_BEFORE);

		super.visitInvokeDynamicInsn(name, descriptor, boot, args);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new InvokeDynamicInsnNode(name, descriptor, boot, args), Inject.SHIFT_AFTER);
	}

	@Override
	public void visitInsn(int opcode)
	{
		for (MixinInjection inj : this.injection)
			inj.visit(this, new InsnNode(opcode), Inject.SHIFT_BEFORE);

		super.visitInsn(opcode);

		for (MixinInjection inj : this.injection)
			inj.visit(this, new InsnNode(opcode), Inject.SHIFT_AFTER);
	}

	@Override
	public void visitLineNumber(int line, Label start)
	{
		super.visitLineNumber(line + this.lineNumber, start);
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
