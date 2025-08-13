package com.example.demo.utils.alert.severities;

import lombok.Getter;

@Getter
public enum TemperatureAlertSeverity {
    LOW("Temperature on range"),
    HIGH("Temperature above optimal range"),
    MEDIUM("Temperature slightly off range");

    private final String description;

    TemperatureAlertSeverity(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}