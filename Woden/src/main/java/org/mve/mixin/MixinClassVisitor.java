package org.mve.mixin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.stream.Stream;

public class MixinClassVisitor extends ClassVisitor
{
	public final MixinInfo[] mixin;

	public MixinClassVisitor(ClassVisitor cv, MixinInfo[] mixin)
	{
		super(Opcodes.ASM9, cv);
		this.mixin = mixin;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
	{
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		MixinInjection[] injections = Stream.of(this.mixin)
			.map(mi -> mi.injection(name, descriptor))
			.flatMap(Stream::of)
			.peek(mi -> mi.preapply(this))
			.toArray(MixinInjection[]::new);
		return new MixinMethodVisitor(mv, access, name, descriptor, signature, exceptions, injections);
	}

	public ClassVisitor visitor()
	{
		return this.cv;
	}
}
