package com.alkl1m.fallback.aspect;

import com.alkl1m.fallback.annotation.Fallback;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {FallbackAspect.class, FallbackAspectTest.TestService.class})
@EnableAspectJAutoProxy
class FallbackAspectTest {

    @Autowired
    private TestService testService;

    @Test
    void testFallbackAspect_whenException_FallbackCalled() {
        testService.shouldThrow = true;
        String result = testService.doSomething();
        assertEquals("fallback result", result);
        assertEquals(1, testService.getFallbackCount());
    }

    @Service
    public static class TestService {
        public boolean shouldThrow = true;
        @Getter
        public int fallbackCount = 0;

        @Fallback(fallbackMethod = "fallback")
        public String doSomething() {
            if (shouldThrow) {
                throw new RuntimeException("Simulated error");
            }
            return "original result";
        }

        public String fallback() {
            fallbackCount++;
            return "fallback result";
        }

    }

}
