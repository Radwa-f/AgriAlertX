package com.example.demo.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        var news = new CaffeineCache("newsCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1000)
                        .build());

        var unsplash = new CaffeineCache("unsplashCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofDays(7))
                        .maximumSize(2000)
                        .build());

        // add to your existing CacheConfig
        var weather = new CaffeineCache("weatherCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(5000)
                        .build());

        var mgr = new SimpleCacheManager();
        mgr.setCaches(List.of(news, unsplash, weather));
        return mgr;
    }
}