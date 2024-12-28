package com.example.demo.utils.crops;


import lombok.Data;

import java.util.List;

@Data
public class CropWeatherRequest {
    private double maxTemp;
    private double minTemp;
    private double maxRain;
    private double minRain;
    private List<String> cropNames;
}