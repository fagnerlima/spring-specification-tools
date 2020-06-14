package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used for between condition.
 * @author Fagner Lima
 * @since 0.1.0
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SpecBetween {

    /**
     * The left property in the between condition.
     *
     * @return
     */
    public String left();

    /**
     * The right property in the between condition.
     *
     * @return
     */
    public String right();

}
