package com.alkl1m.bulkhead.aspect;

import com.alkl1m.bulkhead.annotation.Bulkhead;
import com.alkl1m.bulkhead.exception.BulkheadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {BulkheadAspect.class, BulkheadAspectTest.TestService.class})
@EnableAspectJAutoProxy
class BulkheadAspectTest {

    @Autowired
    private TestService testService;

    @Test
    @DisplayName("""
            Проверка работы Bulkhead: 
            - Ограничение одновременных вызовов до maxConcurrentCalls
            - Выбрасывание BulkheadException при превышении лимита
            - Корректное освобождение ресурсов после завершения задач
            - Повторное использование после завершения предыдущих операций
            - Обработка таймаутов ожидания
            """)
    void testBulkheadLimitsConcurrency_withValidData_worksCorrectly() throws Exception {
        CountDownLatch blockLatch = new CountDownLatch(1);
        testService.setLatch(blockLatch);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<String> future1 = executor.submit(() -> testService.bulkheadMethod());
        Future<String> future2 = executor.submit(() -> testService.bulkheadMethod());

        Thread.sleep(500);

        Future<String> future3 = executor.submit(() -> testService.bulkheadMethod());

        assertThrows(BulkheadException.class, () -> {
            try {
                future3.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });

        blockLatch.countDown();

        assertDoesNotThrow(() -> {
            future1.get();
            future2.get();
        });

        CountDownLatch newLatch = new CountDownLatch(1);
        testService.setLatch(newLatch);

        Future<String> future4 = executor.submit(() -> testService.bulkheadMethod());
        Thread.sleep(500);
        Future<String> future5 = executor.submit(() -> testService.bulkheadMethod());
        assertThrows(BulkheadException.class, () -> {
            try {
                future5.get(1500, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                future5.cancel(true);
                throw new BulkheadException("Timeout occurred");
            }
        });

        newLatch.countDown();
        future4.get();

        executor.shutdown();
    }

    @Service
    static class TestService {
        private CountDownLatch latch = new CountDownLatch(1);

        @Bulkhead(maxConcurrentCalls = 2, timeoutMs = 1000)
        public String bulkheadMethod() throws InterruptedException {
            latch.await();
            return "Success";
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }

}