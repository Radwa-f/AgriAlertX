//
//  WeatherModels.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import Foundation

struct WeatherResponse: Codable {
    let daily: Daily
    let hourly: Hourly
}

struct Daily: Codable {
    let temperatureMax: [Double]
    let temperatureMin: [Double]
    let precipitationSum: [Double]

    enum CodingKeys: String, CodingKey {
        case temperatureMax = "temperature_2m_max"
        case temperatureMin = "temperature_2m_min"
        case precipitationSum = "precipitation_sum"
    }
}

struct Hourly: Codable {
    let precipitation: [Double]
    let time: [String]

    enum CodingKeys: String, CodingKey {
        case precipitation
        case time
    }
}

struct MyAlert: Identifiable {
    let id = UUID()
    let title: String
    let reason: String
    let description: String
    let severity: String
}
