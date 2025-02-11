package com.alkl1m.bulkhead.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для аспекта bulkhead.
 *
 * @author AlKl1M
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bulkhead {

    int maxConcurrentCalls() default 2;

    long timeoutMs() default 1000;

}
