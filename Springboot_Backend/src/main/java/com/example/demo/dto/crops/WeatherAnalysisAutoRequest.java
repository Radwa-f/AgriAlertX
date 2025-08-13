package com.example.demo.dto.crops;


import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeatherAnalysisAutoRequest {
    private Double latitude;   // optional if you resolve from user
    private Double longitude;  // optional if you resolve from user
    private List<String> cropNames;
}

