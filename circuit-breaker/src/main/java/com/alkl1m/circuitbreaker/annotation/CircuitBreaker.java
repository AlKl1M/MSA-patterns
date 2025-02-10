package com.alkl1m.circuitbreaker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для реализации паттерна "Circuit Breaker" на методах.
 * <p>
 * Позволяет автоматически прерывать выполнение метода при превышении заданного порога ошибок
 * или при превышении таймаута выполнения.
 *
 * @author AlKl1M
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CircuitBreaker {

    /**
     * Максимальное количество допускаемых ошибок перед активацией Circuit Breaker.
     *
     * @return значение порога ошибок (по умолчанию 3)
     */
    int failuteThreshold() default 3;

    /**
     * Максимальное время выполнения метода в миллисекундах до срабатывания таймаута.
     *
     * @return значение таймаута в миллисекундах (по умолчанию 5000)
     */
    long timeout() default 5000;

}
