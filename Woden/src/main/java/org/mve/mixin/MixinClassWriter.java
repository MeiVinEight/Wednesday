package org.mve.mixin;

import org.objectweb.asm.ClassWriter;

public class MixinClassWriter extends ClassWriter
{
	public final MixinEngine engine;

	public MixinClassWriter(int flags, MixinEngine engine)
	{
		super(flags);
		this.engine = engine;
	}

	@Override
	protected ClassLoader getClassLoader()
	{
		return this.engine;
		// return super.getClassLoader();
	}
}
