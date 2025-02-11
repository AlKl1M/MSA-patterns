package com.alkl1m.circuitbreaker.aspect;

import com.alkl1m.circuitbreaker.annotation.CircuitBreaker;
import com.alkl1m.circuitbreaker.exception.CircuitBreakerOpenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {CircuitBreakerAspectTest.TestConfig.class, CircuitBreakerAspect.class})
@EnableAspectJAutoProxy
class CircuitBreakerAspectTest {

    @Configuration
    static class TestConfig {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Service
    static class TestService {
        private boolean shouldFail = true;

        @CircuitBreaker(failureThreshold = 3, timeout = 1000)
        public String execute() {
            if (shouldFail) {
                throw new RuntimeException("Simulated failure");
            }
            return "Success";
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
    }

    @Autowired
    private TestService testService;

    @Test
    void testCircuitBreakerOpensAfterThreshold() {
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, testService::execute);
        }

        assertThrows(CircuitBreakerOpenException.class, testService::execute);
    }

    @Test
    void testCircuitBreaker_ResetsAfterTimeout() {
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, testService::execute);
        }

        testService.setShouldFail(false);

        await().atMost(5, SECONDS).pollDelay(1, SECONDS).untilAsserted(() -> {
            assertEquals("Success", testService.execute());
        });
    }

    @Test
    void testCircuitBreaker_HalfOpenStateFailure() {
        testService.setShouldFail(true);
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, testService::execute);
        }

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertThrows(RuntimeException.class, testService::execute);
        });

        assertThrows(CircuitBreakerOpenException.class, testService::execute);
    }
}