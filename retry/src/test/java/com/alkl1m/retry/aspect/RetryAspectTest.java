package com.alkl1m.retry.aspect;

import com.alkl1m.retry.annotation.Retryable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {RetryAspect.class, RetryAspectTest.TestService.class})
@EnableAspectJAutoProxy
class RetryAspectTest {

    @Autowired
    private TestService testService;

    @Test
    void whenMethodSucceedsAfterRetries_thenRetriesThreeTimes() {
        testService.resetCounter();
        try {
            testService.retryThreeTimes();
        } catch (Exception e) {
            fail("Should not throw exception");
        }
        assertEquals(3, testService.getCounter());
    }

    @Test
    void whenMethodAlwaysFails_thenThrowsAfterMaxAttempts() {
        testService.resetCounter();
        assertThrows(Exception.class, () -> testService.alwaysFail());
        assertEquals(3, testService.getCounter());
    }

    @Test
    public void whenExceptionNotRetryable_thenNoRetry() {
        testService.resetCounter();
        assertThrows(RuntimeException.class, () -> testService.retryOnSpecificException());
        assertEquals(1, testService.getCounter());
    }

    @Test
    public void whenRetryWithBackoff_thenDelayBetweenRetries() throws Exception {
        testService.resetCounter();
        long startTime = System.currentTimeMillis();
        assertThrows(Exception.class, () -> testService.retryWithBackoff());
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration >= 2 * 500);
    }

    @Service
    static class TestService {
        private int counter = 0;

        @Retryable(maxAttempts = 3, backoff = 500)
        public void retryThreeTimes() throws Exception {
            counter++;
            if (counter < 3) {
                throw new Exception("Simulated error");
            }
        }

        @Retryable(maxAttempts = 3, backoff = 500)
        public void alwaysFail() throws Exception {
            counter++;
            throw new Exception("Always failing");
        }

        @Retryable(maxAttempts = 2, retryOn = {IllegalArgumentException.class})
        public void retryOnSpecificException() {
            counter++;
            throw new RuntimeException("Not retryable");
        }

        @Retryable(maxAttempts = 3, backoff = 500)
        public void retryWithBackoff() throws Exception {
            counter++;
            throw new Exception("Simulated error for backoff");
        }

        public int getCounter() {
            return counter;
        }

        public void resetCounter() {
            counter = 0;
        }
    }

}
