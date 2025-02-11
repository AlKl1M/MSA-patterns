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

        @CircuitBreaker(failuteThreshold = 3, timeout = 1000)
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
    void testCircuitBreaker_ResetsAfterTimeout() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, testService::execute);
        }

        testService.setShouldFail(false);

        Thread.sleep(1500);

        assertEquals("Success", testService.execute());
    }

    @Test
    void testCircuitBreaker_HalfOpenStateFailure() throws InterruptedException {
        testService.setShouldFail(true);
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, testService::execute);
        }

        Thread.sleep(5000);

        assertThrows(RuntimeException.class, testService::execute);

        assertThrows(CircuitBreakerOpenException.class, testService::execute);
    }

}
