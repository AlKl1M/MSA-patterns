package com.alkl1m.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {

    int maxAttempts() default 3;

    long backoff() default 1000;

    Class<? extends Throwable>[] retryOn() default {Exception.class};

}
