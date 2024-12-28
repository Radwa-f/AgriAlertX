//
//  CropsModels.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import Foundation

struct WeatherAnalysisRequest: Codable {
    let maxTemp: Double
    let minTemp: Double
    let maxRain: Double
    let minRain: Double
    let cropNames: [String]
}

struct CropAnalysisResponse: Codable {
    let cropAnalyses: [String: CropAnalysis]
    let errors: [String]
}

struct CropAnalysis: Codable {
    let overallSeverity: String
    let alerts: [CropsAlert]
    let recommendations: [Recommendation]
    let insights: [String]
    var imageURL: String? // Make this property optional
}


struct CropsAlert: Codable {
    let title: String
    let message: String
}

struct Recommendation: Codable {
    let message: String
}

struct UnsplashPhotoResponse: Codable {
    let urls: Urls
}

struct Urls: Codable {
    let regular: String
}
