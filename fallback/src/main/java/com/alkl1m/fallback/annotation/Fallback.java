package com.alkl1m.fallback.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для аспекта фоллбека. Содержит fallbackMethod() - метод для фоллбека.
 *
 * @author AlKl1M
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    String fallbackMethod();

}
