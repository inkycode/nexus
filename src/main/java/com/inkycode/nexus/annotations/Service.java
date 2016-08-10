package com.inkycode.nexus.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.inkycode.nexus.Auto;

@Retention(CLASS)
@Target(TYPE)
public @interface Service {
    Class<?> value() default Auto.class;

    Class<?> factory() default Auto.class;
}
