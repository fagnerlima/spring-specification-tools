package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.fagnerlima.springspecificationtools.SpecOperation;

/**
 * Used for single conditions.
 * @author Fagner Lima
 * @since 0.1.0
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface SpecField {

    /**
     * @return The name of the field.
     */
    public String value() default "";

    /**
     * @return The operation of the query.
     */
    public SpecOperation operation() default SpecOperation.EQUAL;

    /**
     * @return If {@code true} and the value is {@code null}, the query will be included.
     */
    public boolean canBeNull() default false;

}
