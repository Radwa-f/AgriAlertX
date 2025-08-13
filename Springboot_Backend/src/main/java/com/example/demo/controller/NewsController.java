package com.example.demo.controller;


import com.example.demo.dto.NewsResponseDto;
import com.example.demo.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<NewsResponseDto> getNews(
            @RequestParam(defaultValue = "ma") String country
    ) {

        String fixedQuery = "agriculture";
        int fixedLimit = 10;

        return ResponseEntity.ok(newsService.search(fixedQuery, country, fixedLimit));
    }
}