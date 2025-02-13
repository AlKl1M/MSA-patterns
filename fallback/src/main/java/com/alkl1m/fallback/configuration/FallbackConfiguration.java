package com.alkl1m.fallback.configuration;

import com.alkl1m.fallback.aspect.FallbackAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfiguration {

    @Bean
    public FallbackAspect fallbackAspect() {
        return new FallbackAspect();
    }

}
