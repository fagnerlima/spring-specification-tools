package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.fagnerlima.springspecificationtools.SpecOperator;

/**
 * Define a new group of conditions.
 * @author Fagner Lima
 * @since 0.1.0
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SpecGroup {

    /**
     * @return The operator used in the group.
     */
    public SpecOperator operator() default SpecOperator.AND;

}
