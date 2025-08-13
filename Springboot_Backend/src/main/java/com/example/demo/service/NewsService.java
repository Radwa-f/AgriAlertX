package com.example.demo.service;


import com.example.demo.dto.NewsItemDto;
import com.example.demo.dto.NewsResponseDto;
import com.example.demo.dto.worldnews.WorldNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final WebClient newsWebClient;

    @Value("${news.api-key}")
    private String apiKey;

    @Cacheable(cacheNames = "newsCache", key = "#query + '_' + #country + '_' + #limit", unless = "#result == null")
    public NewsResponseDto search(String query, String country, int limit) {
        WorldNewsResponse ext = newsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search-news")
                        .queryParam("text", Optional.ofNullable(query).orElse("agriculture"))
                        .queryParam("source-country", Optional.ofNullable(country).orElse("ma"))
                        .queryParam("number", limit)
                        .queryParam("api-key", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(WorldNewsResponse.class)
                .block();

        List<NewsItemDto> items = Optional.ofNullable(ext)
                .map(WorldNewsResponse::getNews)
                .orElseGet(List::of)
                .stream()
                .map(n -> NewsItemDto.builder()
                        .title(Optional.ofNullable(n.getTitle()).orElse("No Title"))
                        .text(n.getText())     // can be null; your adapter handles it
                        .url(n.getUrl())
                        .image(n.getImage())
                        .build())
                .collect(Collectors.toList());

        return NewsResponseDto.builder().news(items).build();
    }
}
