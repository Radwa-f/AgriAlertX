package com.example.demo.utils.alert.severities;

import lombok.Getter;

@Getter
public enum TemperatureAlertSeverity {
    LOW("Temperature below optimal range"),
    HIGH("Temperature above optimal range");

    private final String description;

    TemperatureAlertSeverity(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}