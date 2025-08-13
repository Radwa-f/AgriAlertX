package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class OpenMeteoWebClientConfig {
    @Bean("meteoWebClient")
    WebClient meteoWebClient(@Value("${meteo.base-url:https://api.open-meteo.com}") String baseUrl) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(8));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

