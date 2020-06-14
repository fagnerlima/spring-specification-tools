package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Define the entity of the Specification.
 * @author Fagner Lima
 * @since 0.1.0
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SpecEntity {

    public Class<? extends Serializable> value();

}
