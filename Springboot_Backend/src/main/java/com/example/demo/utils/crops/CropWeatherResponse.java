package com.example.demo.utils.crops;

import com.example.demo.model.Alert;
import com.example.demo.model.AlertSeverity;
import com.example.demo.model.Recommendation;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CropWeatherResponse {
    private String id;
    private Map<String, CropAnalysis> cropAnalyses;
    private List<String> errors;  // Added this field

    public String getRequestId() {
        return this.cropAnalyses.toString();
    }

    public void setRequestId(String s) {
        this.id = s;

    }


    @Data
    public static class CropAnalysis {
        private AlertSeverity overallSeverity;
        private List<Alert> alerts;
        private List<Recommendation> recommendations;
        private List<String> insights;
    }
}

