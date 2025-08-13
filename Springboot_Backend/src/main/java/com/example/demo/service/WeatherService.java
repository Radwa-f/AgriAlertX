package com.example.demo.service;


import com.example.demo.dto.weather.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WebClient meteoWebClient;

    @Cacheable(
            cacheNames = "weatherCache",
            key = "T(java.util.Objects).hash(#lat, #lon, 'v1','t2m,precip')",
            sync = true
    )
    public WeatherResponse getForecast(double lat, double lon) {
        return meteoWebClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                        .queryParam("hourly", "precipitation")
                        .queryParam("timezone", "auto")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .block();
    }

    /** Compute next-day hourly precip min/max, using hourly timestamps (YYYY-MM-DDThh:mm). */
    public double[] nextDayRainExtrema(WeatherResponse wr) {
        if (wr == null || wr.getHourly() == null
                || wr.getHourly().getTime() == null || wr.getHourly().getTime().isEmpty()) return new double[]{0,0};

        List<String> times = wr.getHourly().getTime();
        List<Double> precip = wr.getHourly().getPrecipitation();

        String firstDate = times.get(0).substring(0, 10);
        // find the earliest timestamp with a different date → that date is “next day”
        Optional<String> nextDateOpt = times.stream()
                .map(t -> t.substring(0,10))
                .filter(d -> !d.equals(firstDate))
                .findFirst();

        if (nextDateOpt.isEmpty()) return new double[]{0,0};
        String nextDate = nextDateOpt.get();

        List<Double> nextDayValues = IntStream.range(0, times.size())
                .filter(i -> times.get(i).startsWith(nextDate))
                .mapToObj(precip::get)
                .toList();

        if (nextDayValues.isEmpty()) return new double[]{0,0};
        double max = nextDayValues.stream().max(Double::compareTo).orElse(0.0);
        double min = nextDayValues.stream().min(Double::compareTo).orElse(0.0);
        return new double[]{max, min};
    }
}

