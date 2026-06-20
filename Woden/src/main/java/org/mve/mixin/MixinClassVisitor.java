package org.mve.mixin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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

	public MixinClassVisitor(ClassVisitor cv, MixinInfo[] mixin)
	{
		super(Opcodes.ASM9, cv);
		this.mixin = mixin;
		for (MixinInfo info : mixin)
			this.mapping.putAll(info.mapping);
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
		super.visit(version, access, name, signature, superName, list.toArray(String[]::new));
		for (MixinInfo info : mixin)
		{
			if (info.node.fields != null)
			{
				for (FieldNode fn : info.node.fields)
				{
					fn.accept(this);
				}
			}
			if (info.node.methods != null)
			{
				for (MethodNode mn : info.node.methods)
				{
					if (MixinEngine.annotation(mn.visibleAnnotations, "Lorg/mve/mixin/Inject;") != null)
						continue;

					String[] exce = null;
					if (mn.exceptions != null)
						exce = mn.exceptions.toArray(String[]::new);
					MethodVisitor mv = this.visitMethod(mn.access, mn.name, mn.desc, mn.signature, exce);
					mn.accept(mv);
				}
			}
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
	{
		String nameAndType = name + ':' + descriptor;
		if (this.method.contains(nameAndType))
			return new EmptyFieldVisitor();

		this.method.add(nameAndType);
		return super.visitField(access, name, descriptor, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
	{
		String nameAndType = name + ':' + descriptor;
		if (this.method.contains(nameAndType))
			return new EmptyMethodVisitor();

		this.method.add(nameAndType);
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		MixinInjection[] injections = Stream.of(this.mixin)
			.map(mi -> mi.injection(name, descriptor))
			.flatMap(Stream::of)
			.peek(mi -> mi.preapply(this))
			.toArray(MixinInjection[]::new);
		return new MixinMethodVisitor(mv, access, name, descriptor, signature, exceptions, injections, this.mapping);
	}

	public ClassVisitor visitor()
	{
		return this.cv;
	}
}
