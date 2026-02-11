package org.mve.mixin;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MixinInfo
{
	public final ClassNode node;
	public final String target;

	public MixinInfo(ClassNode node, String target)
	{
		this.node = node;
		this.target = target;
	}

	public void shadow(MixinClassVisitor mcv)
	{

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
			String invokeMethod = null;
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

				invokeMethod = (String) values.get("method");
				invokeMethod = invokeMethod == null ? "" : invokeMethod;

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
