package com.alkl1m.circuitbreaker.configuration;

import com.alkl1m.circuitbreaker.aspect.CircuitBreakerAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Класс автоконфигурации
 *
 * @author AlKl1M
 */
@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerAspect circuitBreakerAspect() {
        return new CircuitBreakerAspect();
    }

}
