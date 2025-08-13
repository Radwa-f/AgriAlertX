
package com.example.demo.service;

import com.example.demo.dto.weather.WeatherResponse;
import com.example.demo.utils.crops.CropWeatherResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MlAnalysisClient {
    private static final Logger log = LoggerFactory.getLogger(MlAnalysisClient.class);

    private final WebClient mlWebClient;

    public CropWeatherResponse analyze(List<String> cropNames, WeatherResponse wr) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("cropNames", cropNames);
        payload.put("weather", toPythonWeather(wr));

        AnalyzeResult res = mlWebClient.post()
                .uri("/analyze")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(AnalyzeResult.class)
                .timeout(Duration.ofSeconds(8))
                .onErrorResume(ex -> {
                    log.error("ML call failed", ex);
                    return Mono.just(new AnalyzeResult()); // empty result; controller can fallback
                })
                .block();

        CropWeatherResponse out = new CropWeatherResponse();
        out.setCropAnalyses(res != null && res.cropAnalyses != null ? res.cropAnalyses : new HashMap<>());
        out.setErrors(res != null && res.errors != null ? res.errors : new ArrayList<>());
        return out;
    }

    // Shape Spring WeatherResponse -> Python model field names
    @SuppressWarnings("unchecked")
    private Map<String, Object> toPythonWeather(WeatherResponse wr) {
        Map<String, Object> weather = new HashMap<>();

        Map<String, Object> daily = new HashMap<>();
        if (wr.getDaily() != null) {
            daily.put("temperature_2m_max", orEmpty(wr.getDaily().getTemperatureMax()));
            daily.put("temperature_2m_min", orEmpty(wr.getDaily().getTemperatureMin()));
            // Python expects precipitation_sum
            if (wr.getDaily().getPrecipitationSum() != null) {
                daily.put("precipitation_sum", orEmpty(wr.getDaily().getPrecipitationSum()));
            }
            // Optional humidity mean if you have it later:
            if (wr.getDaily().getRelativeHumidity2mMean() != null) {
                daily.put("relative_humidity_2m_mean", orEmpty(wr.getDaily().getRelativeHumidity2mMean()));
            }
        }
        weather.put("daily", daily);

        Map<String, Object> hourly = new HashMap<>();
        if (wr.getHourly() != null) {
            if (wr.getHourly().getPrecipitation() != null) {
                hourly.put("precipitation", orEmpty(wr.getHourly().getPrecipitation()));
            }
            if (wr.getHourly().getTime() != null) {
                hourly.put("time", orEmptyStr(wr.getHourly().getTime()));
            }
            // Optional: if you later add humidity by hour
            if (wr.getHourly().getRelativeHumidity2m() != null) {
                hourly.put("relative_humidity_2m", orEmpty(wr.getHourly().getRelativeHumidity2m()));
            }
        }
        weather.put("hourly", hourly);

        return weather;
    }

    private List<Double> orEmpty(List<Double> in) { return in != null ? in : Collections.emptyList(); }
    private List<String> orEmptyStr(List<String> in) { return in != null ? in : Collections.emptyList(); }

    @Data
    public static class AnalyzeResult {
        private Map<String, CropWeatherResponse.CropAnalysis> cropAnalyses;
        private List<String> errors;
    }
}
