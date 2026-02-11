package org.mve.mixin;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MixinInjection
{
	public static int mixinNameId = 0;
	public final MixinInfo info;
	public final MethodNode node;
	public final String target;
	public final int at;
	public final String method;
	public final int ordinal;
	public final int shift;
	public final String shadowName;
	public int instruction = 0;
	public int index = 0;
	public boolean applied = false;

	public MixinInjection(MixinInfo info, MethodNode methodNode, String target, int at, String method, int ordinal, int shift)
	{
		this.info = info;
		this.node = methodNode;
		this.target = target;
		this.at = at;
		this.method = method;
		this.ordinal = ordinal;
		this.shift = shift;
		this.shadowName = "mixin$" + methodNode.name + '$' + (mixinNameId++);
	}

	public void preapply(MixinClassVisitor mcv)
	{
		ClassVisitor vis = mcv.visitor();
		MethodVisitor mv = vis.visitMethod(
			this.node.access,
			this.shadowName,
			this.node.desc,
			this.node.signature,
			this.node.exceptions.toArray(String[]::new)
		);
		this.node.accept(mv);
	}

	public void visit(MixinMethodVisitor visitor, AbstractInsnNode node, int shift)
	{
		this.instruction++;
		if (this.applied)
			return;
		if (this.at == Inject.AT_HEAD)
		{
			if (node == null)
				this.apply(visitor);
			return;
		}
		else if (this.at == Inject.AT_INVOKE)
		{
			if (!(node instanceof MethodInsnNode mnode))
				return;
			if (shift != this.shift)
				return;
			String desc = 'L' + mnode.owner + ';' + mnode.name + mnode.desc;
			if (!desc.equals(this.method))
				return;
			if (this.index != this.ordinal)
			{
				this.index++;
				return;
			}
		}
		else
			return;

		// Inject
		this.apply(visitor);
	}

	public void apply(MixinMethodVisitor visitor)
	{
		int isStatic = (this.node.access & Opcodes.ACC_STATIC) >> 3;
		MethodVisitor mv = visitor.mv();
		if (isStatic == 0)
			mv.visitVarInsn(Opcodes.ALOAD, 0);
		Type[] argumentTypes = Type.getArgumentTypes(this.node.desc);
		for (int ai = 0; ai < argumentTypes.length - 1; ++ai)
		{
			Type at = argumentTypes[ai];
			int aii = ai;
			if (isStatic == 0)
				aii++;
			switch (at.getSort())
			{
				case Type.BYTE:
				case Type.SHORT:
				case Type.INT:
				case Type.BOOLEAN:
				case Type.CHAR:
					mv.visitVarInsn(Opcodes.ILOAD, aii);
					break;
				case Type.LONG:
					mv.visitVarInsn(Opcodes.LLOAD, aii);
					break;
				case Type.FLOAT:
					mv.visitVarInsn(Opcodes.FLOAT, aii);
					break;
				case Type.DOUBLE:
					mv.visitVarInsn(Opcodes.DLOAD, aii);
					break;
				case Type.OBJECT:
					mv.visitVarInsn(Opcodes.ALOAD, aii);
			}
		}
		String callback = Callback.class.getName().replace('.', '/');
		mv.visitTypeInsn(Opcodes.NEW, callback);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, callback, "<init>", "()V", false);
		mv.visitInsn(Opcodes.DUP);
		mv.visitVarInsn(Opcodes.ASTORE, visitor.varialeTable.length);
		int opc = Opcodes.INVOKEVIRTUAL;
		if (isStatic == 1)
			opc = Opcodes.INVOKESTATIC;
		mv.visitMethodInsn(opc, this.info.target, this.shadowName, this.node.desc, false);
		Type returnType = Type.getReturnType(visitor.descriptor);
		Label label = new Label();
		mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
		mv.visitFieldInsn(Opcodes.GETFIELD, callback, "cancelled", "Z");
		mv.visitJumpInsn(Opcodes.IFEQ, label);
		switch (returnType.getSort())
		{
			case Type.VOID:
				mv.visitInsn(Opcodes.RETURN);
				break;
			case Type.BOOLEAN:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
				mv.visitInsn(Opcodes.IRETURN);
				break;
			case Type.CHAR:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
				mv.visitInsn(Opcodes.IRETURN);
				break;
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
				mv.visitInsn(Opcodes.IRETURN);
				break;
			case Type.FLOAT:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
				mv.visitInsn(Opcodes.FRETURN);
				break;
			case Type.LONG:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
				mv.visitInsn(Opcodes.LRETURN);
				break;
			case Type.DOUBLE:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
				mv.visitInsn(Opcodes.DRETURN);
				break;
			default:
				mv.visitVarInsn(Opcodes.ALOAD, visitor.varialeTable.length);
				mv.visitFieldInsn(Opcodes.GETFIELD, callback, "returning", "Ljava/lang/Object;");
				mv.visitTypeInsn(Opcodes.CHECKCAST, returnType.getDescriptor());
				mv.visitInsn(Opcodes.ARETURN);
				break;
		}
		mv.visitLabel(label);
		this.applied = true;
	}
}
