package com.example.demo.service;

import com.example.demo.dto.image.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnsplashService {

    private final WebClient unsplashWebClient;

    @Value("${unsplash.access-key}")
    private String accessKey;

    // Cache by normalized query to soften rate limits
    @Cacheable(cacheNames = "unsplashCache", key = "#queryNormalized", unless = "#result == null")
    public ImageResponse getRandom(String queryNormalized) {
        // Unsplash: GET /photos/random?query=...
        Map<?,?> resp = unsplashWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/photos/random")
                        .queryParam("query", queryNormalized)
                        .build())
                .header("Authorization", "Client-ID " + accessKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Extract urls.regular safely
        String url = null;
        if (resp != null && resp.get("urls") instanceof Map<?,?> urls) {
            Object regular = urls.get("regular");
            if (regular != null) url = regular.toString();
        }
        return new ImageResponse(url);
    }

    public static String normalizeQuery(String cropName) {
        if (cropName == null) return "agriculture";
        String q = cropName.trim();
        if (q.equalsIgnoreCase("Coffee")) return "coffee beans";
        if (q.equalsIgnoreCase("Rice")) return "rice grains";
        return q;
    }
}

