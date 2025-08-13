package com.example.demo.controller;

import com.example.demo.dto.image.ImageResponse;
import com.example.demo.service.UnsplashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImagesController {

    private final UnsplashService unsplashService;

    // GET /api/images/random?query=Maize
    @GetMapping("/random")
    public ResponseEntity<ImageResponse> random(@RequestParam String query) {
        String normalized = UnsplashService.normalizeQuery(query);
        return ResponseEntity.ok(unsplashService.getRandom(normalized));
    }

    // Convenience for crops: /api/images/crop?name=Maize
    @GetMapping("/crop")
    public ResponseEntity<ImageResponse> crop(@RequestParam("name") String cropName) {
        String normalized = UnsplashService.normalizeQuery(cropName);
        return ResponseEntity.ok(unsplashService.getRandom(normalized));
    }
}
