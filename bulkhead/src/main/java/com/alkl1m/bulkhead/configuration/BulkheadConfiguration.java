package com.alkl1m.bulkhead.configuration;

import com.alkl1m.bulkhead.aspect.BulkheadAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BulkheadConfiguration {

    @Bean
    public BulkheadAspect bulkheadAspect() {
        return new BulkheadAspect();
    }

}
