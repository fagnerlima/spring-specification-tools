package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author Fagner Lima
 * @since 0.1.0
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SpecPeriod {

    /**
     * The name of the field that represents the start date
     *
     * @return
     */
    public String start();

    /**
     * The name of the field that represents the end date
     *
     * @return
     */
    public String end();

}
