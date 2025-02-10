package com.alkl1m.circuitbreaker.state;

import com.alkl1m.circuitbreaker.enums.CircuitState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CircuitBreakerState {
    private CircuitState state = CircuitState.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime;
    private final int failureThreshold;
    private final long timeout;

    public synchronized void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
    }

    public synchronized void reset() {
        failureCount = 0;
        state = CircuitState.CLOSED;
    }

    public synchronized boolean shouldOpen() {
        return failureCount >= failureThreshold;
    }

    public synchronized boolean shouldTryReset() {
        return (System.currentTimeMillis() - lastFailureTime) > timeout;
    }

}
