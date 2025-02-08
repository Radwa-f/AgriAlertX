package com.example.demo.service;

import com.example.demo.utils.alert.severities.RainfallAlertSeverity;
import com.example.demo.utils.alert.severities.TemperatureAlertSeverity;
import com.example.demo.model.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class CropService {

    private static final Logger logger = LoggerFactory.getLogger(CropService.class);

    private static final Map<String, Crops> cropData = new HashMap<>();

    static {

        cropData.put("Rice", new Crops("Rice", 15, 27, 100, 150));
        cropData.put("Wheat", new Crops("Wheat", 12, 25, 25, 75));
        cropData.put("Maize", new Crops("Maize", 15, 27, 65, 125));
        cropData.put("Millets", new Crops("Millets", 20, 35, 25, 75));
        cropData.put("Bajra (Pearl Millet)", new Crops("Bajra (Pearl Millet)", 25, 35, 25, 60));
        cropData.put("Pulses (Kharif)", new Crops("Pulses (Kharif)", 20, 27, 25, 60));
        cropData.put("Lentil (Rabi)", new Crops("Lentil (Rabi)", 15, 25, 25, 50));
        cropData.put("Oilseeds", new Crops("Oilseeds", 15, 30, 30, 50));
        cropData.put("Groundnut", new Crops("Groundnut", 20, 30, 50, 75));
        cropData.put("Sugarcane", new Crops("Sugarcane", 20, 35, 85, 165));
        cropData.put("Sugar beet", new Crops("Sugar beet", 10, 25, 25, 50));
        cropData.put("Cotton", new Crops("Cotton", 18, 27, 60, 110));
        cropData.put("Tea", new Crops("Tea", 15, 35, 100, 250));
        cropData.put("Coffee", new Crops("Coffee", 15, 28, 125, 225));
        cropData.put("Cocoa", new Crops("Cocoa", 18, 35, 100, 250));
        cropData.put("Rubber", new Crops("Rubber", 27, 27, 150, 250));
        cropData.put("Jute", new Crops("Jute", 25, 35, 150, 250));
        cropData.put("Flax", new Crops("Flax", 10, 20, 15, 20));
        cropData.put("Coconut", new Crops("Coconut", 27, 27, 100, 250));
        cropData.put("Oil-palm", new Crops("Oil-palm", 27, 33, 250, 400));
        cropData.put("Clove", new Crops("Clove", 25, 35, 200, 250));
        cropData.put("Black Pepper", new Crops("Black Pepper", 15, 40, 200, 300));
        cropData.put("Cardamom", new Crops("Cardamom", 10, 35, 150, 400));
        cropData.put("Turmeric", new Crops("Turmeric", 20, 30, 150, 250));
    }

    private CropService(){

    }
    public static String checkWeatherForCrop(String cropName, double maxTemp, double minTemp, double maxRain, double minRain) {
        Crops crop = cropData.get(cropName);
        if (crop == null) {
            return "Crop not found in our database.";
        }

        WeatherAnalysisResult result = new WeatherAnalysisResult();

        // Perform analysis for temperature and rainfall
        analyzeTemperature(crop, maxTemp, minTemp, result); // Modify the temperature analysis
        analyzeRainfall(crop, maxRain, minRain, result); // Modify the rainfall analysis

        // Generate and return a detailed report
        return generateDetailedReport(cropName, result);
    }

    private static void analyzeTemperature(Crops crop, double maxTemp, double minTemp, WeatherAnalysisResult result) {
        double tempDeviationMax = calculateDeviation(maxTemp, crop.getMinTemp(), crop.getMaxTemp());
        double tempDeviationMin = calculateDeviation(minTemp, crop.getMinTemp(), crop.getMaxTemp());

        // Combine temperature analysis
        if (maxTemp > crop.getMaxTemp() || minTemp > crop.getMaxTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.HIGH);
            handleHighTemperature(Math.max(maxTemp, minTemp), crop, result);
        } else if (maxTemp < crop.getMinTemp() || minTemp < crop.getMinTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.LOW);
            handleLowTemperature(Math.min(maxTemp, minTemp), crop, result);
        }

        // Add growth impact insights
        result.addInsight(String.format("Max temperature deviation: %.1f%%, Min temperature deviation: %.1f%%",
                Math.abs(tempDeviationMax) * 10, Math.abs(tempDeviationMin) * 10));
    }

    private static void analyzeRainfall(Crops crop, double maxRain, double minRain, WeatherAnalysisResult result) {
        double rainDeviationMax = calculateDeviation(maxRain, crop.getMinRain(), crop.getMaxRain());
        double rainDeviationMin = calculateDeviation(minRain, crop.getMinRain(), crop.getMaxRain());

        // Combine rainfall analysis
        if (maxRain > crop.getMaxRain() || minRain > crop.getMaxRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.HIGH);
            handleHighRainfall(Math.max(maxRain, minRain), crop, result);
        } else if (maxRain < crop.getMinRain() || minRain < crop.getMinRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.LOW);
            handleLowRainfall(Math.min(maxRain, minRain), crop, result);
        }

        // Add moisture impact insights
        result.addInsight(String.format("Max rainfall deviation: %.1f%%, Min rainfall deviation: %.1f%%",
                Math.abs(rainDeviationMax) * 10, Math.abs(rainDeviationMin) * 10));
    }

    // Add this method to ensure no duplicate recommendations
    private static void addRecommendation(WeatherAnalysisResult result, String recommendation) {
        if (!result.getRecommendations().contains(recommendation)) {
            result.getRecommendations().add(recommendation);
        }
    }

    // Update the handler methods to use the new addRecommendation method
    private static void handleHighTemperature(double userTemp, Crops crop, WeatherAnalysisResult result) {
        if (userTemp > crop.getMaxTemp() + 5) {
            addRecommendation(result, "Extreme heat warning. Implement emergency cooling strategies.");
            addRecommendation(result, "Use shade nets, increase irrigation, and avoid midday activities.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            addRecommendation(result, "Protect crops from heat stress.");
            addRecommendation(result, "Provide temporary shade and increase watering frequency.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleLowTemperature(double userTemp, Crops crop, WeatherAnalysisResult result) {
        if (userTemp < crop.getMinTemp() - 5) {
            addRecommendation(result, "Extreme cold detected. Use frost blankets or greenhouse protection.");
            addRecommendation(result, "Consider using heating systems for sensitive crops.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            addRecommendation(result, "Implement frost protection measures.");
            addRecommendation(result, "Cover crops with mulch to retain soil warmth.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleHighRainfall(double userRain, Crops crop, WeatherAnalysisResult result) {
        if (userRain > crop.getMaxRain() + 50) {
            addRecommendation(result, "Flood risk detected. Implement immediate drainage solutions.");
            addRecommendation(result, "Prepare for potential crop relocation or protection.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            addRecommendation(result, "Ensure proper drainage to prevent waterlogging.");
            addRecommendation(result, "Use raised beds or improve soil drainage.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleLowRainfall(double userRain, Crops crop, WeatherAnalysisResult result) {
        if (userRain < crop.getMinRain() - 50) {
            addRecommendation(result, "Severe drought conditions detected. Immediate irrigation required.");
            addRecommendation(result, "Consider emergency water conservation and crop protection measures.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            addRecommendation(result, "Implement water conservation techniques.");
            addRecommendation(result, "Use drip irrigation or micro-sprinkler systems.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }
    private static double calculateDeviation(double currentValue, double minValue, double maxValue) {
        if (currentValue < minValue) {
            return (minValue - currentValue) / minValue;
        } else if (currentValue > maxValue) {
            return (currentValue - maxValue) / maxValue;
        }
        return 0;
    }

    private static List<Alert> generateAlerts(WeatherAnalysisResult result, String cropName) {
        List<Alert> alerts = new ArrayList<>();

        // Temperature Alerts
        for (TemperatureAlertSeverity tempAlert : result.getTemperatureAlerts()) {
            Alert alert = new Alert();
            AlertType alertType = determineAlertType(tempAlert);
            alert.setType(alertType);
            alert.setSeverity(result.getSeverity());

            switch (tempAlert) {
                case HIGH:
                    alert.setTitle("High Temperature Alert");
                    alert.setMessage(String.format("Temperature exceeds optimal range for %s. Risk of heat stress.", cropName));
                    break;
                case LOW:
                    alert.setTitle("Low Temperature Alert");
                    alert.setMessage(String.format("Temperature below optimal range for %s. Risk of cold damage.", cropName));
                    break;
                default:
                    continue;
            }
            alerts.add(alert);
        }

        // Rainfall Alerts
        for (RainfallAlertSeverity rainAlert : result.getRainfallAlerts()) {
            Alert alert = new Alert();
            AlertType alertType = determineAlertType(rainAlert);
            alert.setType(alertType);
            alert.setSeverity(result.getSeverity());

            switch (rainAlert) {
                case HIGH:
                    alert.setTitle("High Rainfall Alert");
                    alert.setMessage(String.format("Rainfall exceeds optimal range for %s. Risk of waterlogging.", cropName));
                    break;
                case LOW:
                    alert.setTitle("Low Rainfall Alert");
                    alert.setMessage(String.format("Rainfall below optimal range for %s. Risk of drought stress.", cropName));
                    break;
                default:
                    continue;
            }
            alerts.add(alert);
        }

        return alerts;
    }

    private static AlertType determineAlertType(TemperatureAlertSeverity severity) {
        switch (severity) {
            case HIGH:
                return AlertType.ERROR;
            case LOW:
                return AlertType.WARNING;
            default:
                return AlertType.INFO;
        }
    }

    private static AlertType determineAlertType(RainfallAlertSeverity severity) {
        switch (severity) {
            case HIGH:
                return AlertType.ERROR;
            case LOW:
                return AlertType.WARNING;
            default:
                return AlertType.INFO;
        }
    }

    private static List<Recommendation> generateRecommendations(WeatherAnalysisResult result) {
        return result.getRecommendations().stream()
                .map(Recommendation::new)
                .collect(Collectors.toList());
    }

    private static String generateDetailedReport(String cropName, WeatherAnalysisResult result) {
        try {
            List<Alert> alerts = generateAlerts(result, cropName);
            List<Recommendation> recommendations = generateRecommendations(result);

            StringBuilder report = new StringBuilder();

            // Title and Crop Identification
            report.append("Weather Analysis Report for ").append(cropName).append("\n\n");

            // Overall Severity
            report.append("Overall Severity: ").append(result.getSeverity()).append("\n\n");

            // Alerts Section
            if (!alerts.isEmpty()) {
                report.append("Alerts:\n");
                alerts.forEach(alert -> {
                    String alertPrefix = getAlertPrefix(alert.getType());
                    report.append(alertPrefix)
                            .append(" ").append(alert.getTitle())
                            .append("\n   ").append(alert.getMessage())
                            .append("\n   Severity: ").append(alert.getSeverity())
                            .append("\n\n");
                });
            }

            // Recommendations Section
            if (!recommendations.isEmpty()) {
                report.append("Recommended Actions:\n");
                recommendations.forEach(rec ->
                        report.append("- ").append(rec.getMessage()).append("\n")
                );
                report.append("\n");
            }

            // Insights Section
            if (!result.getInsights().isEmpty()) {
                report.append("Additional Insights:\n");
                result.getInsights().forEach(insight ->
                        report.append("- ").append(insight).append("\n")
                );
            }

            return report.toString();

        } catch (Exception e) {
            logger.info("Error generating report: {}", e.getMessage());
            return String.format("Error generating detailed report for %s. Please check the system logs.", cropName);
        }
    }

    private static String getAlertPrefix(AlertType type) {
        switch (type) {
            case ERROR:
                return "❌";
            case WARNING:
                return "⚠️";
            case INFO:
                return "ℹ️";
            default:
                return "-";
        }
    }

    @Getter
    static class WeatherAnalysisResult {
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
}
