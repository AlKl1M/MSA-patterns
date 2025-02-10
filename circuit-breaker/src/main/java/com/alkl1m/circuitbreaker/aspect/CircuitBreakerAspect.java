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

@Aspect
@Component
public class CircuitBreakerAspect {

    private final Map<String, CircuitBreakerState> circuitStates = new ConcurrentHashMap<>();

    @Pointcut("@annotation(circuitBreaker)")
    public void circuitBrakerPointcut(CircuitBreaker circuitBreaker) {}

    @Around(value = "circuitBrakerPointcut(circuitBreaker)", argNames = "joinPoint, circuitBreaker")
    public Object handleCircuitBreaker(ProceedingJoinPoint joinPoint,
                                       CircuitBreaker circuitBreaker) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        CircuitBreakerState state = circuitStates.computeIfAbsent(methodName,
                k -> new CircuitBreakerState(
                        circuitBreaker.failuteThreshold(),
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
