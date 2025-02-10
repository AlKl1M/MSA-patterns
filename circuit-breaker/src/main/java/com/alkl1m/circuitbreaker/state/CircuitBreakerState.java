package com.alkl1m.circuitbreaker.state;

import com.alkl1m.circuitbreaker.enums.CircuitState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Класс для хранения и управления состоянием Circuit Breaker для конкретного метода.
 * <p>
 * Отслеживает количество ошибок, время последнего сбоя и управляет переключением состояний:
 * <ul>
 *   <li>CLOSED - нормальный режим работы</li>
 *   <li>OPEN - режим аварийного отключения</li>
 * </ul>
 *
 * <p>Потокобезопасная реализация с использованием synchronized методов.
 *
 * @author AlKl1M
 * @see CircuitState
 */
@Getter
@Setter
@RequiredArgsConstructor
public class CircuitBreakerState {
    private CircuitState state = CircuitState.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime;
    private final int failureThreshold;
    private final long timeout;

    /**
     * Фиксирует новую ошибку в текущем состоянии.
     * <p>
     * Атомарно выполняет:
     * <ol>
     *   <li>Увеличивает счетчик ошибок на 1</li>
     *   <li>Обновляет время последней ошибки до текущего</li>
     * </ol>
     */
    public synchronized void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
    }

    /**
     * Сбрасывает состояние до начального.
     * <p>
     * Атомарно выполняет:
     * <ol>
     *   <li>Обнуляет счетчик ошибок</li>
     *   <li>Устанавливает состояние CLOSED</li>
     * </ol>
     */
    public synchronized void reset() {
        failureCount = 0;
        state = CircuitState.CLOSED;
    }

    /**
     * Проверяет необходимость перехода в состояние OPEN.
     *
     * @return true если количество ошибок достигло порогового значения
     */
    public synchronized boolean shouldOpen() {
        return failureCount >= failureThreshold;
    }

    /**
     * Проверяет возможность попытки сброса состояния.
     *
     * @return true если с момента последней ошибки прошло больше времени, чем заданный timeout
     */
    public synchronized boolean shouldTryReset() {
        return (System.currentTimeMillis() - lastFailureTime) > timeout;
    }

}
