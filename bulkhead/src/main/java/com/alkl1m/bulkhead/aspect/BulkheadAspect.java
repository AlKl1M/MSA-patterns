package com.alkl1m.bulkhead.aspect;

import com.alkl1m.bulkhead.annotation.Bulkhead;
import com.alkl1m.bulkhead.exception.BulkheadException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Аспект bulkhead. Изолирует ресурсы и предотвращает каскадные сбои.
 * Является Thread/Concurrency bulkhead. Ограничивает кол-во одновременных
 * запросов к методу/сервису.
 *
 * @author AlKl1M
 */
@Aspect
@Component
public class BulkheadAspect {

    private final ConcurrentHashMap<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    @Pointcut("@annotation(bulkhead)")
    public void bulkheadPointcut(Bulkhead bulkhead) {
    }

    /**
     * Основная логика управления параллелизмом.
     *
     * <p>Алгоритм работы:
     * <ol>
     *   <li>Определение сигнатуры вызываемого метода</li>
     *   <li>Генерация уникального ключа для метода</li>
     *   <li>Получение или создание семафора с параметрами из аннотации</li>
     *   <li>Попытка захвата семафора с таймаутом</li>
     *   <li>Обработка успешного/неуспешного захвата</li>
     *   <li>Освобождение ресурса в блоке finally</li>
     * </ol>
     *
     * @param joinPoint точка соединения (перехваченный метод)
     * @param bulkhead  аннотация с параметрами конфигурации
     * @return результат выполнения оригинального метода
     * @throws Throwable         исключения из оригинального метода
     * @throws BulkheadException если превышен лимит ожидания или concurrent-запросов
     */
    @Around(value = "bulkheadPointcut(bulkhead)", argNames = "joinPoint, bulkhead")
    public Object manageConcurrency(ProceedingJoinPoint joinPoint, Bulkhead bulkhead) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String key = method.getDeclaringClass().getName() + "#" + method.getName();

        Semaphore semaphore = semaphores.computeIfAbsent(key,
                k -> new Semaphore(bulkhead.maxConcurrentCalls()));

        boolean acquired = false;
        try {
            acquired = semaphore.tryAcquire(bulkhead.timeoutMs(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new BulkheadException("Too many concurrent requests - please try again later");
            }
            return joinPoint.proceed();
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

}
