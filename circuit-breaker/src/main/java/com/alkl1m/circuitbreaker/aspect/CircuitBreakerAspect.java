package com.alkl1m.circuitbreaker.aspect;

import com.alkl1m.circuitbreaker.annotation.CircuitBreaker;
import com.alkl1m.circuitbreaker.enums.CircuitState;
import com.alkl1m.circuitbreaker.exception.CircuitBreakerOpenException;
import com.alkl1m.circuitbreaker.state.CircuitBreakerState;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Аспект, реализующий паттерн "Circuit Breaker" для методов, помеченных аннотацией {@link CircuitBreaker}.
 * <p>
 * Обеспечивает автоматическое отслеживание ошибок и таймаутов выполнения методов, управляя их состоянием:
 * <ul>
 *   <li>CLOSED - нормальная работа</li>
 *   <li>OPEN - сервис временно недоступен</li>
 *   <li>HALF_OPEN - пробный режим после таймаута</li>
 * </ul>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Отслеживание количества ошибок через {@link CircuitBreaker#failuteThreshold()}</li>
 *   <li>Контроль времени выполнения через {@link CircuitBreaker#timeout()}</li>
 *   <li>Автоматическое переключение состояний</li>
 *   <li>Потокобезопасная реализация с использованием синхронизации</li>
 * </ul>
 *
 * @author AlKl1M
 * @see CircuitBreaker
 * @see CircuitBreakerState
 */
@Aspect
@Component
public class CircuitBreakerAspect {

    private final Map<String, CircuitBreakerState> circuitStates = new ConcurrentHashMap<>();

    @Pointcut("@annotation(circuitBreaker)")
    public void circuitBrakerPointcut(CircuitBreaker circuitBreaker) {
    }

    /**
     * Обрабатывает вызов метода с Circuit Breaker.
     * <p>
     * Логика работы:
     * <ol>
     *   <li>Проверяет текущее состояние Circuit Breaker</li>
     *   <li>При OPEN состоянии проверяет возможность перехода в HALF_OPEN</li>
     *   <li>Выполняет целевой метод</li>
     *   <li>При успехе - сбрасывает состояние</li>
     *   <li>При ошибке - обновляет счетчик ошибок и меняет состояние при необходимости</li>
     * </ol>
     *
     * @param joinPoint      точка соединения для получения информации о методе
     * @param circuitBreaker экземпляр аннотации CircuitBreaker
     * @return результат выполнения целевого метода
     * @throws Throwable                   в случае ошибки выполнения метода или при OPEN состоянии Circuit Breaker
     * @throws CircuitBreakerOpenException если Circuit Breaker находится в OPEN состоянии
     */
    @Around(value = "circuitBrakerPointcut(circuitBreaker)", argNames = "joinPoint, circuitBreaker")
    public Object handleCircuitBreaker(ProceedingJoinPoint joinPoint,
                                       CircuitBreaker circuitBreaker) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        CircuitBreakerState state = circuitStates.computeIfAbsent(methodName,
                k -> new CircuitBreakerState(
                        circuitBreaker.failureThreshold(),
                        circuitBreaker.timeout()
                ));

        synchronized (state) {
            if (state.getState() == CircuitState.OPEN) {
                if (state.shouldTryReset()) {
                    state.setState(CircuitState.HALF_OPEN);
                } else {
                    throw new CircuitBreakerOpenException("Service unavailable");
                }
            }
        }

        try {
            Object result = joinPoint.proceed();
            synchronized (state) {
                state.reset();
            }
            return result;
        } catch (Exception e) {
            synchronized (state) {
                state.recordFailure();
                if (state.shouldOpen()) {
                    state.setState(CircuitState.OPEN);
                } else if (state.getState() == CircuitState.HALF_OPEN) {
                    state.setState(CircuitState.OPEN);
                }
            }
            throw e;
        }
    }

}
