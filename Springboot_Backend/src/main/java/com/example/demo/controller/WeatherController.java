package com.example.demo.controller;


import com.example.demo.dto.weather.WeatherResponse;
import com.example.demo.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) {
        return ResponseEntity.ok(weatherService.getForecast(lat, lon));
    }
}

