package com.alkl1m.retry.aspect;

import com.alkl1m.retry.annotation.Retryable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RetryAspect {

    @Pointcut("@annotation(retryable)")
    public void retryPointcut(Retryable retryable) {
    }

    @Around(value = "retryPointcut(retryable)", argNames = "joinPoint, retryable")
    public Object retryOperation(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        int maxAttempts = retryable.maxAttempts();
        long backoff = retryable.backoff();
        Class<? extends Throwable>[] retryExceptions = retryable.retryOn();

        int attempt = 0;
        Throwable lastException;

        do {
            attempt++;
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                lastException = e;
                if (!shouldRetry(e, retryExceptions)) {
                    throw e;
                }
                if (attempt < maxAttempts) {
                    Thread.sleep(backoff);
                }
            }
        } while (attempt < maxAttempts);

        throw lastException;
    }

    private boolean shouldRetry(Throwable error, Class<? extends Throwable>[] retryExceptions) {
        return Arrays.stream(retryExceptions)
                .anyMatch(exClass -> exClass.isAssignableFrom(error.getClass()));
    }

}
