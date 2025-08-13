
package com.example.demo.dto.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeatherResponse {
    private Daily daily;
    private Hourly hourly;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Daily {
        @JsonProperty("temperature_2m_max")
        private List<Double> temperatureMax;
        @JsonProperty("temperature_2m_min")
        private List<Double> temperatureMin;
        @JsonProperty("precipitation_sum")
        private List<Double> precipitationSum;
        @JsonProperty("relative_humidity_2m_mean")
        private List<Double> relativeHumidity2mMean;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Hourly {
        private List<Double> precipitation;
        private List<String> time; // ISO timestamps
        @JsonProperty("relative_humidity_2m")
        private List<Double> relativeHumidity2m;
    }
}

