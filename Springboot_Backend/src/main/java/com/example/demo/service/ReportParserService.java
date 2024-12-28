package com.example.demo.service;

import com.example.demo.utils.crops.CropWeatherResponse;
import com.example.demo.model.Alert;
import com.example.demo.model.AlertSeverity;
import com.example.demo.model.AlertType;
import com.example.demo.model.Recommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportParserService {

    private static final Logger logger = LoggerFactory.getLogger(ReportParserService.class);
    private static final int MAX_INPUT_LENGTH = 10000; // Limit input length to avoid overflow

    public CropWeatherResponse.CropAnalysis parseReport(String report) {
        if (report == null || report.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty report cannot be parsed");
        }

        CropWeatherResponse.CropAnalysis analysis = new CropWeatherResponse.CropAnalysis();

        // Parse each section
        analysis.setOverallSeverity(parseOverallSeverity(report));
        analysis.setAlerts(parseAlerts(report));
        analysis.setRecommendations(parseRecommendations(report));
        analysis.setInsights(parseInsights(report));

        return analysis;
    }

    public AlertSeverity parseOverallSeverity(String report) {
        Pattern severityPattern = Pattern.compile("Overall Severity:\\s+(\\w+)", Pattern.CANON_EQ);
        Matcher matcher = severityPattern.matcher(report);

        if (matcher.find()) {
            try {
                return AlertSeverity.valueOf(matcher.group(1));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid severity found in report: {}", matcher.group(1));
                return AlertSeverity.LOW;
            }
        }
        return AlertSeverity.LOW;
    }

    public List<Alert> parseAlerts(String report) {
        List<Alert> alerts = new ArrayList<>();
        Pattern alertPattern = Pattern.compile(
                "(❌|⚠️|ℹ️)\\s+([^\\n]{1,100})\\n\\s+([^\\n]{1,200})\\n\\s+Severity:\\s+(LOW|MEDIUM|HIGH)", Pattern.CANON_EQ
        );
        Matcher matcher = alertPattern.matcher(report);

        while (matcher.find()) {
            Alert alert = new Alert();

            // Set alert type based on emoji
            switch (matcher.group(1)) {
                case "❌":
                    alert.setType(AlertType.ERROR);
                    break;
                case "⚠️":
                    alert.setType(AlertType.WARNING);
                    break;
                case "ℹ️":
                default:
                    alert.setType(AlertType.INFO);
                    break;
            }

            alert.setTitle(matcher.group(2).trim());
            alert.setMessage(matcher.group(3).trim());
            try {
                alert.setSeverity(AlertSeverity.valueOf(matcher.group(4).trim()));
            } catch (IllegalArgumentException e) {
                alert.setSeverity(AlertSeverity.LOW);
            }

            alerts.add(alert);
        }

        return alerts;
    }

    // Unified parsing for sections to avoid repetition
    private List<String> parseSection(String report, String sectionHeader) {
        List<String> results = new ArrayList<>();

        if (report.length() > MAX_INPUT_LENGTH) {
            report = report.substring(0, MAX_INPUT_LENGTH); // Truncate long input
        }

        Pattern sectionPattern = Pattern.compile(sectionHeader + ":\\n((?:-[^\\n]+\\n?)+)", Pattern.CANON_EQ);
        Matcher sectionMatcher = sectionPattern.matcher(report);

        if (sectionMatcher.find()) {
            Matcher itemMatcher = Pattern.compile("-\\s+([^\\n]+)", Pattern.CANON_EQ).matcher(sectionMatcher.group(1));

            while (itemMatcher.find()) {
                results.add(itemMatcher.group(1).trim());
            }
        }

        return results;
    }

    public List<Recommendation> parseRecommendations(String report) {
        List<String> recommendations = parseSection(report, "Recommended Actions");
        List<Recommendation> result = new ArrayList<>();

        for (String rec : recommendations) {
            result.add(new Recommendation(rec));
        }
        return result;
    }

    public List<String> parseInsights(String report) {
        return parseSection(report, "Additional Insights");
    }
}
