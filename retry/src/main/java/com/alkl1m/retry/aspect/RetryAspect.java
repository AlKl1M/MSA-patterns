package com.alkl1m.retry.aspect;

import com.alkl1m.retry.annotation.Retryable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Аспект для реализации паттерна Retry с использованием Spring AOP.
 * <p>
 * Обеспечивает повторное выполнение метода при возникновении указанных исключений
 * с возможностью настройки задержки между попытками.
 * </p>
 *
 * <p><b>Основной функционал:</b></p>
 * <ul>
 *   <li>Многократный вызов метода при ошибках</li>
 *   <li>Настраиваемое количество попыток выполнения</li>
 *   <li>Экспоненциальная задержка между попытками (backoff)</li>
 *   <li>Фильтрация исключений для повтора</li>
 * </ul>
 *
 * <p><b>Принцип работы:</b></p>
 * <ol>
 *   <li>Перехват методов с аннотацией {@link Retryable}</li>
 *   <li>Повтор выполнения метода до достижения maxAttempts</li>
 *   <li>Выдержка паузы между попытками при backoff > 0</li>
 *   <li>Проверка типа исключения через shouldRetry</li>
 * </ol>
 *
 * @author AlKl1M
 */
@Aspect
@Component
public class RetryAspect {

    /**
     * Точка среза для методов с аннотацией {@link Retryable}.
     *
     * @param retryable экземпляр аннотации с параметрами повтора
     */
    @Pointcut("@annotation(retryable)")
    public void retryPointcut(Retryable retryable) {
    }

    /**
     * Основная логика обработки повторных попыток выполнения метода.
     *
     * <p><b>Алгоритм работы:</b></p>
     * <ol>
     *   <li>Извлечение параметров из аннотации</li>
     *   <li>Цикл попыток выполнения до достижения maxAttempts</li>
     *   <li>Обработка исключений:
     *     <ul>
     *       <li>Проверка типа исключения через shouldRetry()</li>
     *       <li>Выдержка паузы при необходимости</li>
     *       <li>Повтор при совпадении типа исключения</li>
     *     </ul>
     *   </li>
     *   <li>Проброс последнего исключения при исчерпании попыток</li>
     * </ol>
     *
     * @param joinPoint точка соединения для доступа к параметрам метода
     * @param retryable экземпляр аннотации {@link Retryable}
     * @return результат выполнения оригинального метода
     * @throws Throwable при ошибках выполнения или исчерпании попыток
     */
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

    /**
     * Проверяет необходимость повтора на основе типа исключения.
     *
     * @param error           возникшее исключение
     * @param retryExceptions массив классов исключений для повтора
     * @return true если исключение входит в список разрешенных для повтора
     * @see Arrays#stream(Object[])
     * @see Class#isAssignableFrom(Class)
     */
    private boolean shouldRetry(Throwable error, Class<? extends Throwable>[] retryExceptions) {
        return Arrays.stream(retryExceptions)
                .anyMatch(exClass -> exClass.isAssignableFrom(error.getClass()));
    }

}
