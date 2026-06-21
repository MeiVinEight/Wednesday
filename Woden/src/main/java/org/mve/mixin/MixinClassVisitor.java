package org.mve.mixin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MixinClassVisitor extends ClassVisitor
{
	public final MixinInfo[] mixin;
	public final Set<String> method = new HashSet<>();
	public final Map<Object, Object> mapping = new HashMap<>();
	public final Set<String> overwrite;
	public final ClassNode target;
	public String name;
	public int lineNumber;

	public MixinClassVisitor(ClassVisitor cv, MixinInfo[] mixin, ClassNode target)
	{
		super(Opcodes.ASM9, cv);
		this.mixin = mixin;
		this.target = target;
		for (MixinInfo info : mixin)
			this.mapping.putAll(info.mapping);
		this.overwrite = this.mapping.values()
			.stream()
			.filter(MethodRef.class::isInstance)
			.map(s -> ((MethodRef) s).nameAndType())
			.collect(Collectors.toSet());
		this.target.accept(this);
		this.overwrite.clear();
		this.lineNumber = MixinEngine.maxLines(this.target, 0);
		for (MixinInfo info : mixin)
		{
			for (FieldNode fn : info.node.fields)
				fn.accept(this);
			for (MethodNode mn : info.node.methods)
			{
				String[] exce = null;
				if (mn.exceptions != null)
					exce = mn.exceptions.toArray(String[]::new);
				MethodVisitor mv = this.visitMethod(mn.access, mn.name, mn.desc, mn.signature, exce);
				mn.accept(mv);
			}
			this.lineNumber = MixinEngine.maxLines(info.node, this.lineNumber);
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		HashSet<String> list = null;
		if (interfaces != null)
			list = Stream.of(interfaces).collect(Collectors.toCollection(HashSet::new));
		if (list == null)
			list = new HashSet<>();
		for (MixinInfo info : mixin)
			if (info.node.interfaces != null)
				list.addAll(info.node.interfaces);
		this.name = name;
		super.visit(version, access, name, signature, superName, list.toArray(String[]::new));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
	{
		String nameAndType = new MethodRef(this.name, name, descriptor).nameAndType();
		if (this.method.contains(nameAndType))
			return new EmptyFieldVisitor();

		this.method.add(nameAndType);
		return super.visitField(access, name, descriptor, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
	{
		String nameAndType = new MethodRef(this.name, name, descriptor).nameAndType();
		if (this.overwrite.contains(nameAndType))
			return new EmptyMethodVisitor();
		if (this.method.contains(nameAndType))
			return new EmptyMethodVisitor();

		this.method.add(nameAndType);
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		return new MixinMethodVisitor(this, mv, access, name, descriptor, signature, exceptions);
	}

	public ClassVisitor visitor()
	{
		return this.cv;
	}
}
