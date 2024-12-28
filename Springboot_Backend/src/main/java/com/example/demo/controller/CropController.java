package com.example.demo.controller;

import com.example.demo.service.CropService;
import com.example.demo.service.ReportParserService;
import com.example.demo.utils.ErrorResponse;
import com.example.demo.utils.crops.CropAnalysisException;
import com.example.demo.utils.crops.CropWeatherRequest;
import com.example.demo.utils.crops.CropWeatherResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crops")
@Validated
public class CropController {

    private static final Logger logger = LoggerFactory.getLogger(CropController.class);
    private final ReportParserService reportParserService;

    @Autowired
    public CropController(ReportParserService reportParserService) {
        this.reportParserService = reportParserService;
    }

    @PostMapping("/weather-analysis")
    public ResponseEntity<Object> analyzeWeatherForCrops(@Valid @RequestBody CropWeatherRequest request) {
        logger.info("Received weather analysis request for crops: {}", request.getCropNames());

        try {
            validateRequest(request);
            CropWeatherResponse response = processWeatherAnalysis(request);

            if (response.getCropAnalyses().isEmpty() && !response.getErrors().isEmpty()) {
                return ResponseEntity.badRequest().body(buildErrorResponse(400, "Analysis Failed", response.getErrors()));
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(400, "Invalid Request", errors));
        } catch (CropAnalysisException e) {
            logger.error("Crop analysis error", e);
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(400, "Crop Analysis Failed", errors));
        } catch (Exception e) {
            logger.error("Unexpected error during weather analysis", e);
            List<String> errors = new ArrayList<>();
            errors.add("An unexpected error occurred.");
            return ResponseEntity.status(500).body(buildErrorResponse(500, "Internal Server Error", errors));
        }
    }

    private CropWeatherResponse processWeatherAnalysis(CropWeatherRequest request) {
        Map<String, CropWeatherResponse.CropAnalysis> analyses = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (String cropName : request.getCropNames()) {
            try {
                CropWeatherResponse.CropAnalysis analysis = analyzeSingleCrop(cropName, request);
                analyses.put(cropName, analysis);
            } catch (Exception e) {
                logger.error("Error analyzing crop: {}", cropName, e);
                errors.add("Failed to analyze crop '" + cropName + "': " + e.getMessage());
            }
        }

        CropWeatherResponse response = new CropWeatherResponse();
        response.setCropAnalyses(analyses);
        response.setErrors(errors);
        return response;
    }

    private CropWeatherResponse.CropAnalysis analyzeSingleCrop(String cropName, CropWeatherRequest request) {
        logger.debug("Analyzing crop: {}", cropName);
        String report = CropService.checkWeatherForCrop(
                cropName, request.getMaxTemp(), request.getMinTemp(), request.getMaxRain(), request.getMinRain()
        );

        if (report == null || report.trim().isEmpty()) {
            throw new CropAnalysisException("No analysis report generated for crop: " + cropName);
        }

        return reportParserService.parseReport(report);
    }

    private void validateRequest(CropWeatherRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getMinTemp() > request.getMaxTemp()) {
            errors.add("Minimum temperature cannot be greater than maximum temperature");
        }
        if (request.getMinRain() > request.getMaxRain()) {
            errors.add("Minimum rainfall cannot be greater than maximum rainfall");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    private ErrorResponse buildErrorResponse(int status, String message, List<String> errors) {
        return new ErrorResponse(LocalDateTime.now(), status, message, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(buildErrorResponse(400, "Validation Error", errors));
    }
}
