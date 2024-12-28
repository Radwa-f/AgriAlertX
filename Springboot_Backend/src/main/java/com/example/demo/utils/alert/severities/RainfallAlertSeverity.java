package com.example.demo.utils.alert.severities;

import lombok.Getter;

@Getter
public enum RainfallAlertSeverity {
    LOW("Insufficient rainfall"),
    HIGH("Excessive rainfall");

    private final String description;

    RainfallAlertSeverity(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
