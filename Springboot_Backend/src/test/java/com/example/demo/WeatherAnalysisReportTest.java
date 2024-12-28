package com.example.demo;

import com.example.demo.utils.alert.reports.WeatherAnalysisReport;
import com.example.demo.utils.alert.reports.WeatherAnalysisResult;
import com.example.demo.utils.alert.severities.RainfallAlertSeverity;
import com.example.demo.utils.alert.severities.TemperatureAlertSeverity;
import com.example.demo.model.Crops;
import com.example.demo.service.CropService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherAnalysisReportTest {

    @Test
    public void testRiceWeatherAnalysis() {
        // Create a crop instance for Rice
        Crops rice = new Crops("Rice", 15, 27, 100, 150);

        // Simulate extreme weather conditions
        double maxTemp = 35; // High temperature
        double minTemp = 10; // Low temperature
        double maxRain = 200; // High rainfall
        double minRain = 50; // Low rainfall

        // Use CropService to analyze weather conditions
        String weatherReport = CropService.checkWeatherForCrop("Rice", maxTemp, minTemp, maxRain, minRain);

        // Print the detailed report
        System.out.println("===== Weather Analysis Report for Rice =====");
        System.out.println(weatherReport);
    }

    @Test
    public void testWheatWeatherAnalysis() {
        // Create a crop instance for Wheat
        Crops wheat = new Crops("Wheat", 12, 25, 25, 75);

        // Simulate moderate weather conditions
        double maxTemp = 22; // Slightly below optimal
        double minTemp = 15; // Within range
        double maxRain = 50; // Low rainfall
        double minRain = 20; // Very low rainfall

        // Use CropService to analyze weather conditions
        String weatherReport = CropService.checkWeatherForCrop("Wheat", maxTemp, minTemp, maxRain, minRain);

        // Print the detailed report
        System.out.println("===== Weather Analysis Report for Wheat =====");
        System.out.println(weatherReport);
    }

    @Test
    public void testManualWeatherAnalysisReport() {
        // Manually create a WeatherAnalysisResult for demonstration
        WeatherAnalysisResult result = new WeatherAnalysisResult();

        // Add some alerts
        result.addTemperatureAlert(TemperatureAlertSeverity.HIGH);
        result.addTemperatureAlert(TemperatureAlertSeverity.LOW);

        result.addRainfallAlert(RainfallAlertSeverity.HIGH);

        // Add recommendations
        result.addRecommendation("Implement emergency cooling strategies");
        result.addRecommendation("Ensure proper drainage");

        // Add insights
        result.addInsight("Temperature significantly deviates from optimal range");
        result.addInsight("Rainfall exceeds crop requirements");

        // Create a sample crop
        Crops crop = new Crops("Sample Crop", 15, 25, 50, 100);

        // Generate and print the report
        String manualReport = WeatherAnalysisReport.generateDetailedReport(crop, result);

        System.out.println("===== Manual Weather Analysis Report =====");
        System.out.println(manualReport);
    }

    @Test
    public void testMultipleCropAnalysis() {
        String[] crops = {"Rice", "Wheat", "Maize", "Millets"};

        for (String cropName : crops) {
            // Simulate varied weather conditions
            double maxTemp = getRandomTemperature(10, 40);
            double minTemp = getRandomTemperature(5, 35);
            double maxRain = getRandomRainfall(0, 250);
            double minRain = getRandomRainfall(0, 200);

            System.out.println("\n===== Weather Analysis for " + cropName + " =====");
            System.out.println("Weather Conditions:");
            System.out.printf("Max Temp: %.2f, Min Temp: %.2f\n", maxTemp, minTemp);
            System.out.printf("Max Rainfall: %.2f, Min Rainfall: %.2f\n", maxRain, minRain);

            // Analyze and print report
            String weatherReport = CropService.checkWeatherForCrop(cropName, maxTemp, minTemp, maxRain, minRain);
            System.out.println(weatherReport);
        }
    }

    // Helper method to generate random temperatures
    private double getRandomTemperature(double min, double max) {
        return min + Math.random() * (max - min);
    }

    // Helper method to generate random rainfall
    private double getRandomRainfall(double min, double max) {
        return min + Math.random() * (max - min);
    }
}
