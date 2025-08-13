// src/main/java/com/example/demo/controller/CropAutoAnalysisController.java
package com.example.demo.controller;

import com.example.demo.dto.crops.WeatherAnalysisAutoRequest;
import com.example.demo.dto.weather.WeatherResponse;
import com.example.demo.service.*;
import com.example.demo.utils.crops.CropWeatherResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/crops/weather-analysis")
@RequiredArgsConstructor
public class CropAutoAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(CropAutoAnalysisController.class);

    private final WeatherService weatherService;
    private final CropService cropService;              // legacy
    private final ReportParserService reportParser;     // legacy
    private final MlAnalysisClient mlAnalysisClient;    // NEW

    @PostMapping("/auto")
    public ResponseEntity<CropWeatherResponse> analyzeAuto(@RequestBody WeatherAnalysisAutoRequest req) {
        if (req.getLatitude() == null || req.getLongitude() == null || req.getCropNames() == null) {
            return ResponseEntity.badRequest().build();
        }

        WeatherResponse wr = weatherService.getForecast(req.getLatitude(), req.getLongitude());
        if (wr == null || wr.getDaily() == null) {
            return ResponseEntity.internalServerError().build();
        }

        // 1) Try ML Python first
        CropWeatherResponse ml = mlAnalysisClient.analyze(req.getCropNames(), wr);
        if (ml != null && ml.getCropAnalyses() != null && !ml.getCropAnalyses().isEmpty()) {
            return ResponseEntity.ok(ml);
        }

        // 2) Fallback to legacy Java analyzer if ML not available / empty
        log.warn("ML result empty or failed; falling back to legacy Java analysis");
        Map<String, CropWeatherResponse.CropAnalysis> analyses = new HashMap<>();
        List<String> errors = new ArrayList<>();

        double maxTemp = safeIndex(wr.getDaily().getTemperatureMax(), 1, 0.0);
        double minTemp = safeIndex(wr.getDaily().getTemperatureMin(), 1, 0.0);
        double[] hh = weatherService.nextDayRainExtrema(wr);
        double maxRain = hh[0];
        double minRain = hh[1];

        for (String crop : req.getCropNames()) {
            try {
                String report = cropService.checkWeatherForCrop(crop, maxTemp, minTemp, maxRain, minRain);
                var analysis = reportParser.parseReport(report);
                analyses.put(crop, analysis);
            } catch (Exception ex) {
                log.error("Legacy analysis error for {}", crop, ex);
                errors.add("Failed to analyze crop '" + crop + "': " + ex.getMessage());
            }
        }
        CropWeatherResponse resp = new CropWeatherResponse();
        resp.setCropAnalyses(analyses);
        resp.setErrors(errors);
        return ResponseEntity.ok(resp);
    }

    private static double safeIndex(java.util.List<Double> list, int idx, double def) {
        return (list != null && list.size() > idx && list.get(idx) != null) ? list.get(idx) : def;
    }
}
