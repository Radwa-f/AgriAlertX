package com.example.demo.utils.alert.reports;

import com.example.demo.model.AlertSeverity;
import com.example.demo.utils.alert.severities.RainfallAlertSeverity;
import com.example.demo.utils.alert.severities.TemperatureAlertSeverity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter

public class WeatherAnalysisResult {
    @Setter
    private AlertSeverity severity = AlertSeverity.LOW;
    private final List<TemperatureAlertSeverity> temperatureAlerts = new ArrayList<>();
    private final List<RainfallAlertSeverity> rainfallAlerts = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();
    private final List<String> insights = new ArrayList<>();


    public void addTemperatureAlert(TemperatureAlertSeverity alert) {
        temperatureAlerts.add(alert);
    }

    public void addRainfallAlert(RainfallAlertSeverity alert) {
        rainfallAlerts.add(alert);
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public void addInsight(String insight) {
        insights.add(insight);
    }



}
