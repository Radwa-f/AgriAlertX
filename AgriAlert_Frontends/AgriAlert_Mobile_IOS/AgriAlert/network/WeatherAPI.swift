//
//  WeatherAPI.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import Alamofire

class WeatherAPI {
    static let shared = WeatherAPI()
    
    private let baseURL = "https://api.open-meteo.com/v1/forecast"
    
    func getWeather(latitude: Double, longitude: Double, completion: @escaping (Result<WeatherResponse, Error>) -> Void) {
        let parameters: [String: Any] = [
            "latitude": latitude,
            "longitude": longitude,
            "daily": "temperature_2m_max,temperature_2m_min,precipitation_sum",
            "hourly": "precipitation",
            "timezone": "auto"
        ]
        
        AF.request(baseURL, method: .get, parameters: parameters).responseDecodable(of: WeatherResponse.self) { response in
            switch response.result {
            case .success(let weatherResponse):
                completion(.success(weatherResponse))
            case .failure(let error):
                print("Failed to fetch weather data: \(error.localizedDescription)")
                if let data = response.data {
                    print("Raw Response: \(String(data: data, encoding: .utf8) ?? "No Data")")
                }
                completion(.failure(error))
            }
        }
    }
}
