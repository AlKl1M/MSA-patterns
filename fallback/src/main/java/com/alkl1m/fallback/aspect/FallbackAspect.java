package com.alkl1m.fallback.aspect;

import com.alkl1m.fallback.annotation.Fallback;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Аспект, реализующий паттерн Fallback с использованием Spring AOP.
 * <p>
 * Основная задача аспекта - перехват исключений при выполнении методов, помеченных аннотацией {@link Fallback},
 * и вызов резервного метода (fallback) в случае возникновения ошибки.
 * </p>
 *
 * <p><b>Требования к резервному методу:</b></p>
 * <ul>
 *   <li>Должен находиться в том же классе, что и исходный метод</li>
 *   <li>Должен иметь одинаковую сигнатуру параметров с исходным методом</li>
 *   <li>Должен возвращать значение совместимого типа с исходным методом</li>
 * </ul>
 *
 * @author AlKl1M
 */
@Aspect
@Component
public class FallbackAspect {

    @Pointcut("@annotation(fallback)")
    public void retryPointcut(Fallback fallback) {
    }

    /**
     * Обработчик логики Fallback вокруг целевого метода.
     *
     * <p><b>Алгоритм работы:</b></p>
     * <ol>
     *   <li>Выполнение оригинального метода</li>
     *   <li>При успешном выполнении - возврат результата</li>
     *   <li>При возникновении исключения:
     *     <ul>
     *       <li>Получение метаданных исходного метода</li>
     *       <li>Поиск резервного метода по имени из аннотации</li>
     *       <li>Вызов резервного метода с оригинальными аргументами</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param joinPoint точка соединения для доступа к параметрам метода
     * @param fallback  экземпляр аннотации {@link Fallback} с параметрами
     * @return результат выполнения оригинального или резервного метода
     * @throws Throwable в случае ошибок при выполнении методов или reflection-вызовах
     */
    @Around(value = "retryPointcut(fallback)", argNames = "joinPoint, fallback")
    public Object handleFallback(ProceedingJoinPoint joinPoint, Fallback fallback) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object target = joinPoint.getTarget();

            Method fallbackMethod = target.getClass().getMethod(fallback.fallbackMethod(), method.getParameterTypes());

            return fallbackMethod.invoke(target, joinPoint.getArgs());
        }
    }

}
