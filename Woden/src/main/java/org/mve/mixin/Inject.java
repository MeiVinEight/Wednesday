package org.mve.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Inject
{
	public static final int AT_HEAD = 0;
	public static final int AT_RETURN = 1;
	public static final int AT_INVOKE = 2;

	public static final int SHIFT_BEFORE = 0;
	public static final int SHIFT_AFTER = 1;

	String value();
	int at() default AT_HEAD;
	String method() default "";
	int ordinal() default 0;
	int shift() default SHIFT_BEFORE;
}
