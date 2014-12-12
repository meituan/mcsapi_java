package com.meituan.mos.shell.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandOption {
	String name();
	Class type() default String.class;
	String action() default "";
	String metavar() default "";
	String help();
}
