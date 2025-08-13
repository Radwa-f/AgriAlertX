package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class UnsplashWebClientConfig {
    @Bean("unsplashWebClient")
    WebClient unsplashWebClient(@Value("${unsplash.base-url}") String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(8))
                .compress(true);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

