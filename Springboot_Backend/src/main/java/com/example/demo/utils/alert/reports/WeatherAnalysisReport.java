package com.example.demo.utils.alert.reports;

import com.example.demo.model.Crops;
import java.util.List;

public class WeatherAnalysisReport {

    private WeatherAnalysisReport(){
    }

    public static String generateDetailedReport(Crops crop, WeatherAnalysisResult result) {
        StringBuilder report = new StringBuilder();
        report.append("Weather Analysis Report for ").append(crop.getName()).append("\n\n");

        // Severity
        report.append("Overall Severity: ").append(result.getSeverity()).append("\n\n");

        // Temperature Alerts
        appendAlerts(report, result.getTemperatureAlerts(), "Temperature Alerts");

        // Rainfall Alerts (assuming it uses the same pattern as Temperature Alerts)
        appendAlerts(report, result.getRainfallAlerts(), "Rainfall Alerts");

        // Recommendations
        appendRecommendations(report, result.getRecommendations());

        // Insights
        appendInsights(report, result.getInsights());

        return report.toString();
    }

    private static <T> void appendAlerts(StringBuilder report, List<T> alerts, String sectionTitle) {
        if (!alerts.isEmpty()) {
            report.append(sectionTitle).append(":\n");
            for (T alert : alerts) {
                report.append("- ").append(alert.toString()).append("\n"); // Calls the toString() method of the alert object
            }
            report.append("\n");
        }
    }

    private static void appendRecommendations(StringBuilder report, List<String> recommendations) {
        if (!recommendations.isEmpty()) {
            report.append("Recommended Actions:\n");
            for (String rec : recommendations) {
                report.append("- ").append(rec).append("\n");
            }
            report.append("\n");
        }
    }

    private static void appendInsights(StringBuilder report, List<String> insights) {
        if (!insights.isEmpty()) {
            report.append("Additional Insights:\n");
            for (String insight : insights) {
                report.append("- ").append(insight).append("\n");
            }
        }
    }
}
