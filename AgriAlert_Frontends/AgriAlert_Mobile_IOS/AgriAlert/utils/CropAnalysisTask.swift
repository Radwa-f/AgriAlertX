//
//  CropAnalysisTask.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 4/1/2025.
//

import Foundation
import UserNotifications

class CropAnalysisTask {
    func performAnalysis(completion: @escaping (Bool) -> Void) {
            // Use KeychainHelper instead of UserDefaults for sensitive data
            guard let token = KeychainHelper.shared.retrieve(key: "JWT_TOKEN") else {
                print("No token found. Aborting analysis.")
                completion(false)
                return
            }
            
            // Get location from secure storage
            guard let latitude = UserDefaults.standard.double(forKey: "USER_LATITUDE") as? Double,
                  let longitude = UserDefaults.standard.double(forKey: "USER_LONGITUDE") as? Double else {
                print("No location found. Aborting analysis.")
                completion(false)
                return
            }
            
            let group = DispatchGroup()
            var weatherData: WeatherResponse?
            var cropAnalysisData: [String: CropAnalysis]?
            var hasError = false
            
            // Fetch weather data
            group.enter()
            fetchWeather(latitude: latitude, longitude: longitude) { result in
                if let data = result {
                    weatherData = data
                } else {
                    hasError = true
                }
                group.leave()
            }
            
            // Wait for all operations to complete
            group.notify(queue: .main) {
                guard !hasError, let weather = weatherData else {
                    completion(false)
                    return
                }
                
                // Fetch crop analysis with weather data
                self.fetchCropAnalysis(token: token, weatherData: weather) { result in
                    if let analysis = result {
                        self.saveCropAnalysisLocally(analysis)
                        self.triggerNotification(for: analysis)
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            }
        }
        
        private func triggerNotification(for analysis: [String: CropAnalysis]) {
            let content = UNMutableNotificationContent()
            content.title = "Crop Analysis Update"
            content.body = self.generateNotificationBody(from: analysis)
            content.sound = .default
            
            // Create trigger for immediate delivery
            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
            
            let request = UNNotificationRequest(
                identifier: UUID().uuidString,
                content: content,
                trigger: trigger
            )
            
            UNUserNotificationCenter.current().add(request) { error in
                if let error = error {
                    print("Failed to schedule notification: \(error.localizedDescription)")
                } else {
                    print("Notification scheduled successfully")
                }
            }
        }
        
    private func generateNotificationBody(from analysis: [String: CropAnalysis]) -> String {
            var criticalAlerts: [String] = []
            
            for (cropName, analysis) in analysis {
                // Check if there are any alerts with high severity
                if analysis.overallSeverity == "HIGH" || analysis.overallSeverity == "CRITICAL" {
                    if let firstAlert = analysis.alerts.first {
                        criticalAlerts.append("\(cropName): \(firstAlert.message)")
                    }
                }
            }
            
            if !criticalAlerts.isEmpty {
                return "Alert: " + criticalAlerts.joined(separator: "; ")
            }
            
            // If no critical alerts, check if there are any recommendations
            var recommendations: [String] = []
            for (cropName, analysis) in analysis {
                if let firstRecommendation = analysis.recommendations.first {
                    recommendations.append("\(cropName): \(firstRecommendation.message)")
                }
            }
            
            if !recommendations.isEmpty {
                return "Recommendations: " + recommendations.joined(separator: "; ")
            }
            
            return "Your crops are doing well"
        }
    private func fetchWeather(latitude: Double, longitude: Double, completion: @escaping (WeatherResponse?) -> Void) {
        WeatherAPI.shared.getWeather(latitude: latitude, longitude: longitude) { result in
            switch result {
            case .success(let weatherData):
                completion(weatherData)
            case .failure(let error):
                print("Failed to fetch weather: \(error.localizedDescription)")
                completion(nil)
            }
        }
    }

    private func fetchCropAnalysis(token: String, weatherData: WeatherResponse, completion: @escaping ([String: CropAnalysis]?) -> Void) {
        let maxTemp = weatherData.daily.temperatureMax.first ?? 0.0
        let minTemp = weatherData.daily.temperatureMin.first ?? 0.0
        let maxRain = weatherData.daily.precipitationSum.max() ?? 0.0
        let minRain = weatherData.daily.precipitationSum.min() ?? 0.0

        let request = WeatherAnalysisRequest(maxTemp: maxTemp, minTemp: minTemp, maxRain: maxRain, minRain: minRain, cropNames: ["Wheat", "Rice"])
        
        APIClient.shared.getCropAnalysis(request: request) { result in
            switch result {
            case .success(let cropAnalysisResponse):
                completion(cropAnalysisResponse.cropAnalyses)
            case .failure(let error):
                print("Failed to fetch crop analysis: \(error.localizedDescription)")
                completion(nil)
            }
        }
    }

    private func saveCropAnalysisLocally(_ cropAnalysis: [String: CropAnalysis]) {
        // Save to local storage (e.g., UserDefaults, CoreData, etc.)
        print("Crop analysis saved locally.")
    }

    
}
