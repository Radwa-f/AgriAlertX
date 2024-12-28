package com.example.demo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Crops {
    private String name;
    private double minTemp;
    private double maxTemp;
    private double minRain;
    private double maxRain;
}
