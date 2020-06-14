package com.github.fagnerlima.springspecificationtools.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.fagnerlima.springspecificationtools.SpecOperator;

@Retention(RUNTIME)
@Target(FIELD)
public @interface SpecGroup {

    public SpecOperator operator() default SpecOperator.AND;

}
