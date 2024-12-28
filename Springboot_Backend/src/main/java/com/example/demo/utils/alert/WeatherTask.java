package com.example.demo.utils.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.model.Location;
import com.example.demo.service.CropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
public class WeatherTask {

    private static final Logger logger = LoggerFactory.getLogger(WeatherTask.class);

    private final AppUserRepository appUserRepository;
    private final CropService cropService;

    public WeatherTask(AppUserRepository appUserRepository, CropService cropService) {
        this.appUserRepository = appUserRepository;
        this.cropService = cropService;
    }

    @Scheduled(cron = "0 0 6 * * ?") // Scheduled at 6 AM daily
    public void fetchWeatherData() {
        List<AppUser> users = appUserRepository.findAll();

        for (AppUser user : users) {
            Location location = user.getLocation();
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                try {
                    // Fetch weather data for the user's location
                    String weatherData = fetchWeatherForLocation(latitude, longitude);

                    // Extract max and min temperatures and rainfall from the fetched weather data
                    double[] temperatures = extractTemperatures(weatherData);
                    double[] rainfall = extractRainfall(weatherData);

                    // Process the weather data for the user
                    processWeatherData(user, temperatures[0], temperatures[1], rainfall[0], rainfall[1]);

                } catch (Exception e) {
                    logger.error("Failed to fetch or process weather data for user {}: {}", user.getEmail(), e.getMessage());
                }
            }
        }
    }

    private String fetchWeatherForLocation(double latitude, double longitude) {
        String url = "https://api.open-meteo.com/v1/forecast";
        String params = String.format(
                "?latitude=%f&longitude=%f&daily=temperature_2m_max,temperature_2m_min,rain_sum&forecast_days=2",
                latitude, longitude);

        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url + params, String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch weather data for location [{}, {}]: {}", latitude, longitude, e.getMessage());
            return null;
        }
    }

    public void processWeatherData(AppUser user, double maxTemperature, double minTemperature, double maxRainfall, double minRainfall) {
        logger.info("Weather data for user {}: Max Temp: {}, Min Temp: {}, Max Rainfall: {}, Min Rainfall: {}",
                user.getEmail(), maxTemperature, minTemperature, maxRainfall, minRainfall);

        for (String cropName : user.getCrops()) {
            String weatherReport = cropService.checkWeatherForCrop(cropName, maxTemperature, minTemperature, maxRainfall, minRainfall);
            logger.info("Weather report for crop {}: {}", cropName, weatherReport);
        }
    }

    private double[] extractTemperatures(String weatherData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(weatherData);
            JsonNode dailyNode = rootNode.path("daily");

            if (dailyNode.isMissingNode() || dailyNode.isEmpty()) {
                throw new IOException("Missing 'daily' node in weather data.");
            }

            double maxTemperature = dailyNode.path("temperature_2m_max").get(0).asDouble();
            double minTemperature = dailyNode.path("temperature_2m_min").get(0).asDouble();

            return new double[]{maxTemperature, minTemperature};
        } catch (IOException e) {
            logger.error("Error parsing temperature data: {}", e.getMessage());
            return new double[]{0.0, 0.0};
        }
    }

    private double[] extractRainfall(String weatherData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(weatherData);
            JsonNode dailyNode = rootNode.path("daily");

            if (dailyNode.isMissingNode() || dailyNode.isEmpty()) {
                throw new IOException("Missing 'daily' node in weather data.");
            }

            JsonNode rainData = dailyNode.path("rain_sum");
            double maxRainfall = rainData.get(0).asDouble(); // Day 1 rainfall
            double minRainfall = rainData.get(1).asDouble(); // Day 2 rainfall

            return new double[]{maxRainfall, minRainfall};
        } catch (IOException e) {
            logger.error("Error parsing rainfall data: {}", e.getMessage());
            return new double[]{0.0, 0.0};
        }
    }
}
