package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.fagnerlima.springspecificationtools.SpecOperation;

@Retention(RUNTIME)
@Target({ FIELD })
public @interface SpecField {

    /**
     * Field name
     */
    public String value() default "";

    public SpecOperation operation() default SpecOperation.EQUAL;

    public boolean canBeNull() default false;

}
