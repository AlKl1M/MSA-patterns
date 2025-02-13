package com.alkl1m.retry.configuration;

import com.alkl1m.retry.aspect.RetryAspect;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.annotation.Bean;

@CacheConfig
public class RetryConfiguration {

    @Bean
    public RetryAspect retryAspect() {
        return new RetryAspect();
    }

}
