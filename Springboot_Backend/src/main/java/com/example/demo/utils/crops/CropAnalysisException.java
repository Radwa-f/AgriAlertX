package com.example.demo.utils.crops;


public class CropAnalysisException extends RuntimeException {
    public CropAnalysisException(String message) {
        super(message);
    }

    public CropAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
