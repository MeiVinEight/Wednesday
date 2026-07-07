package org.mve.mixin;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MixinInfo
{
	public final ClassNode node;
	public final String target;
	public final Map<Object, Object> mapping;

	public MixinInfo(ClassNode node, String target)
	{
		this.node = node;
		this.target = target;
		this.mapping = new HashMap<>();
		List<InvokeDynamicInsnNode> dynamics = new LinkedList<>();
		if (this.node.fields != null)
		{
			for (Iterator<FieldNode> it = this.node.fields.iterator(); it.hasNext();)
			{
				FieldNode fn = it.next();
				String name = fn.name;
				if (MixinEngine.annotation(fn.visibleAnnotations, "Lorg/mve/mixin/Shadow;") == null)
					name = "mixin$" + name + '$' + UUID.randomUUID().toString().toUpperCase().replace("-", "");
				else
					it.remove();
				MethodRef key = new MethodRef(node.name, fn.name, fn.desc);
				MethodRef val = new MethodRef(this.target, name, fn.desc);
				this.mapping.put(key, val);
				fn.name = name;
			}
		}
		if (this.node.methods != null)
		{
			for (Iterator<MethodNode> it = this.node.methods.iterator(); it.hasNext();)
			{
				MethodNode mn = it.next();
				if ("<init>".equals(mn.name))
				{
					boolean inject = MixinEngine.annotation(mn.visibleAnnotations, "Lorg/mve/mixin/Inject;") != null;
					boolean shadow = MixinEngine.annotation(mn.visibleAnnotations, "Lorg/mve/mixin/Shadow;") != null;
					boolean flag = true;
					flag &= !inject;
					flag &= !shadow;
					if (flag)
					{
						it.remove();
						continue;
					}
				}
				if (MixinEngine.annotation(mn.visibleAnnotations, "Lorg/mve/mixin/Shadow;") != null)
				{
					it.remove();
					continue;
				}
				String name = mn.name;
				if (MixinEngine.annotation(mn.visibleAnnotations, "Lorg/mve/mixin/Overwrite;") == null)
				{
					if ("<init>".equals(name))
						name = "init";
					name = "mixin$" + name + "$" + UUID.randomUUID().toString().toUpperCase();
					byte[] bytes0 = name.getBytes(StandardCharsets.UTF_8);
					byte[] bytes1 = new byte[bytes0.length];
					int idx = 0;
					for (byte b : bytes0)
					{
						if (MixinEngine.validNamechar(b))
							bytes1[idx++] = b;
					}
					name = new String(bytes1, 0, idx, StandardCharsets.UTF_8);
				}
				MethodRef key = new MethodRef(node.name, mn.name, mn.desc);
				MethodRef val = new MethodRef(this.target, name, mn.desc);
				this.mapping.put(key, val);
				mn.name = name;
				for (AbstractInsnNode insn : mn.instructions)
				{
					if (insn instanceof InvokeDynamicInsnNode dynamic)
						dynamics.add(dynamic);
				}
			}
		}
		for (InvokeDynamicInsnNode dyn : dynamics)
		{
			Handle boot = dyn.bsm;
			Object[] args = dyn.bsmArgs;
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
				dyn.desc = dyn.desc.replaceAll('L' + this.node.name + ';', 'L' + this.target + ';');
			}
		}
	}

	public MixinInjection[] injection(String name, String desc)
	{
		String methodKey = name + desc;
		List<MixinInjection> injections = new LinkedList<>();
		for (MethodNode method : node.methods)
		{
			if (method.visibleAnnotations == null)
				continue;

			int at = Inject.AT_HEAD;
			String invokeMethod;
			int ordinal = 0;
			int shift = Inject.SHIFT_BEFORE;
			for (AnnotationNode anno : method.visibleAnnotations)
			{
				if (!Type.getDescriptor(Inject.class).equals(anno.desc))
					continue;
				if (anno.values == null)
					continue;

				Map<String, Object> values = new HashMap<>();
				for (int i = 0; i < anno.values.size(); i += 2)
					values.put((String) anno.values.get(i), anno.values.get(i + 1));

				if (!methodKey.equals(values.get("value")))
					continue;

				Number annoNum = (Number) values.get("at");
				if (annoNum != null)
					at = annoNum.intValue();

				invokeMethod = (String) values.getOrDefault("method", "");

				annoNum = (Number) values.get("ordinal");
				if (annoNum != null)
					ordinal = annoNum.intValue();

				annoNum = (Number) values.get("shift");
				if (annoNum != null)
					shift = annoNum.intValue();

				injections.add(new MixinInjection(this, method, methodKey, at, invokeMethod, ordinal, shift));
			}
		}
		return injections.toArray(MixinInjection[]::new);
	}
}
