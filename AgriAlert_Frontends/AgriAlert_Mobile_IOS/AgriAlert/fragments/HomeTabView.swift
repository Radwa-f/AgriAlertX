//
//  HomeTabView.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import SwiftUI
import CoreLocation

struct HomeTabView: View {
    @State private var weatherData: WeatherResponse? = nil
    @ObservedObject var locationManager = LocationManager()
    @State private var alerts: [MyAlert] = [
        MyAlert(title: "Frost Warning", reason: "Low Temperature", description: "Protect your crops from frost.", severity: "HIGH"),
        MyAlert(title: "Heavy Rain", reason: "High Precipitation", description: "Prepare drainage for heavy rain.", severity: "HIGH"),
        MyAlert(title: "Pest Alert", reason: "Pest Activity", description: "Use pesticide for pest control.", severity: "HIGH"),
    ]
    
    var body: some View {
        VStack {
            // Weather Card
            if let weatherData = weatherData {
                WeatherCardView(locationManager: locationManager, weatherData: weatherData)
                
                    .padding(.vertical, 0) // Reduce space between the top bar and the weather card
                    .onTapGesture {
                        print("Weather card tapped")
                    }
            } else {
                Text("Fetching weather...")
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding()
            }
            Spacer()
                .frame(height: 100)

            // Daily Insights Title
            Text("My Daily Insights - Today")
                .font(.headline)
                .padding(.horizontal)
                .foregroundColor(Color(hex: "#2c3e50"))

            // Daily Insights Horizontal List
            ScrollView(.horizontal, showsIndicators: false) {
                
                HStack(spacing: 16) {
                    
                    ForEach(alerts) { alert in
                        AlertCardView(alert: alert)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 10)
            }
        }
        .onAppear {
            locationManager.requestLocation()
            fetchWeather()
        }
    }
    
    func fetchWeather() {
        if let latitude = locationManager.latitude, let longitude = locationManager.longitude {
            print("Real-time Location: Latitude = \(latitude), Longitude = \(longitude)")
            fetchWeatherData(latitude: latitude, longitude: longitude)
        } else if let storedLatitude = UserDefaults.standard.value(forKey: "USER_LATITUDE") as? Double,
                  let storedLongitude = UserDefaults.standard.value(forKey: "USER_LONGITUDE") as? Double {
            print("Using Stored Location: Latitude = \(storedLatitude), Longitude = \(storedLongitude)")
            fetchWeatherData(latitude: storedLatitude, longitude: storedLongitude)
        } else {
            print("No location available.")
        }
    }

    func fetchWeatherData(latitude: Double, longitude: Double) {
        WeatherAPI.shared.getWeather(latitude: latitude, longitude: longitude) { result in
            switch result {
            case .success(let weatherResponse):
                print("Daily Max Temp: \(weatherResponse.daily.temperatureMax)")
                print("Hourly Precipitation: \(weatherResponse.hourly.precipitation)")
                DispatchQueue.main.async {
                    self.weatherData = weatherResponse // Update weatherData to trigger UI re-render
                }
            case .failure(let error):
                print("Error fetching weather: \(error.localizedDescription)")
            }
        }
    }
}


// Weather Card View
struct WeatherCardView: View {
    @ObservedObject var locationManager: LocationManager // Use dependency injection
    let weatherData: WeatherResponse
    @State private var cityName: String = "Unknown City"

    var body: some View {
        ZStack {
            // Card Background with a Gradient
            LinearGradient(
                gradient: Gradient(colors: [Color.blue.opacity(0.8), Color.white.opacity(0.4)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .cornerRadius(16)
            .shadow(color: Color.gray.opacity(0.4), radius: 8, x: 0, y: 4)

            // Weather Details
            VStack(spacing: 10) {
                // Weather Icon
                Image(determineWeatherIcon())
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)
                    .padding(.top, 10)

                // Dynamic City Name
                Text(cityName)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "#2c3e50"))

                // Weather Condition
                Text(determineWeatherCondition())
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))

                // Temperature
                Text("\(weatherData.daily.temperatureMax[0], specifier: "%.1f")°C")
                    .font(.system(size: 42, weight: .bold, design: .rounded))
                    .foregroundColor(Color(hex: "#2c3e50"))
            }
            .padding()
        }
        .frame(height: 180) // Adjust card height
        .padding(.horizontal, 16)
        .padding(.top, -40) // Move card closer to the top bar
        .onAppear {
            if let latitude = locationManager.latitude, let longitude = locationManager.longitude {
                fetchCityName(latitude: latitude, longitude: longitude)
            } else {
                print("Latitude and Longitude not available")
            }
        }
    }

    func determineWeatherCondition() -> String {
        let minTemp = weatherData.daily.temperatureMin[0]
        let precipitation = weatherData.daily.precipitationSum[0]

        if precipitation > 0 {
            return "Rainy"
        } else if minTemp > 25 {
            return "Sunny"
        } else {
            return "Cloudy"
        }
    }

    func determineWeatherIcon() -> String {
        let maxTemp = weatherData.daily.temperatureMax[0]
        let precipitation = weatherData.daily.precipitationSum[0]

        if precipitation > 0 {
            return "ic_rainy" // Replace with rainy weather icon name
        } else if maxTemp > 25 {
            return "ic_sunny" // Replace with sunny weather icon name
        } else {
            return "ic_cloudy" // Replace with cloudy weather icon name
        }
    }

    func fetchCityName(latitude: Double, longitude: Double) {
        let geocoder = CLGeocoder()
        let location = CLLocation(latitude: latitude, longitude: longitude)
        geocoder.reverseGeocodeLocation(location) { placemarks, error in
            if let error = error {
                print("Error fetching city name: \(error.localizedDescription)")
                cityName = "Unknown City"
                return
            }

            if let placemark = placemarks?.first {
                cityName = placemark.locality ?? "Unknown City"
            } else {
                cityName = "Unknown City"
            }
        }
    }
}


// Alert Card View
struct AlertCardView: View {
    let alert: MyAlert

    var body: some View {
        VStack(alignment: .leading) {
            
            Text(alert.title)
                .font(.headline)
                .foregroundColor(.red)

            Text(alert.reason)
                .font(.subheadline)
                .foregroundColor(.gray)

            Text(alert.description)
                .font(.footnote)
                .foregroundColor(.blue)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(10)
        .shadow(radius: 2)
    }
}


