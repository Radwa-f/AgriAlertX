//
//  HomeTabView.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import SwiftUI
import CoreLocation
import Kingfisher

struct HomeTabView: View {
    @State private var weatherData: WeatherResponse? = nil
    @State private var userLocation: UserLocation? = nil
    @State private var alerts: [MyAlert] = []
    @State private var crops: [Crop] = []
    @State private var isLoadingCrops: Bool = true

    @State private var alertMessage: AlertItem? = nil
    
    @State private var isLoading: Bool = true
    @State private var errorMessage: String = ""
    
    private let cropHarvestSeasons: [String: String] = [
        
            "Rice": "October-November",
            "Wheat": "Through March-April",
            "Maize": "Through July-August",
            "Millets": "Through July-August",
            "Bajra": "Through June-July",
            "Pulses": "August-September",
            "Lentil": "November-December",
            "Oilseeds": "Through July-August",
            "Groundnut": "August-September",
            "Sugarcane": "November-December",
            "Sugar beet": "November-December",
            "Cotton": "September-October",
            "Tea": "Throughout the year",
            "Coffee": "September-October",
            "Cocoa": "October-November",
            "Rubber": "Throughout the year",
            "Jute": "August-September",
            "Flax": "Through June-July",
            "Coconut": "Throughout the year",
            "Oil-palm": "Throughout the year",
            "Clove": "November-December",
            "Black Pepper": "December-January",
            "Cardamom": "September-October",
            "Turmeric": "December-January"


        ]
    

    var body: some View {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // My Crops Section
                    Text("My Crops - What I'm raising")
                        .font(.headline)
                        .foregroundColor(Color(hex: "#2c3e50"))
                        .padding(.horizontal)

                    if isLoadingCrops {
                                        ProgressView("Loading Crops...")
                                            .padding()
                                    } else if crops.isEmpty {
                                        Text("No crops to display.")
                                            .foregroundColor(.gray)
                                            .padding()
                                    } else {
                                        ScrollView(.horizontal, showsIndicators: false) {
                                            HStack(spacing: 16) {
                                                ForEach(crops) { crop in
                                                    CropsCardView(crop: crop)
                                                }
                                            }
                                            .padding(.horizontal)
                                            .padding(.bottom, 16) // Add bottom padding for spacing
                                        }
                                    }


                    // Weather Section
                    Text("Forecast - My Current Weather")
                        .font(.headline)
                        .foregroundColor(Color(hex: "#2c3e50"))
                        .padding(.horizontal)

                    if let weatherData = weatherData, let location = userLocation {
                        WeatherCardView(weatherData: weatherData, location: location)
                            .padding(.vertical, 8)
                    } else {
                        Text("Fetching weather...")
                            .foregroundColor(Color(hex: "#2c3e50"))
                            .padding()
                    }

                    // Daily Insights Section
                    Text("My Daily Insights - Today")
                        .font(.headline)
                        .foregroundColor(Color(hex: "#2c3e50"))
                        .padding(.horizontal)

                    if isLoading {
                        ProgressView("Loading Alerts...")
                            .padding()
                    } else if !errorMessage.isEmpty {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .padding()
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 16) {
                                ForEach(alerts) { alert in
                                    AlertCardView(alert: alert)
                                }
                            }
                            .padding(.horizontal)
                            .padding(.bottom, 16) // Add bottom padding for spacing
                        }
                    }
                }
                .padding(.top, 8) // Reduced top padding
            }
            .onAppear {
                fetchCrops()
                fetchWeather()
                fetchUserDataAndAnalyzeCropsForAlerts()
            }
            .alert(item: $alertMessage) { alert in
                Alert(title: Text("Error"), message: Text(alert.message), dismissButton: .default(Text("OK")))
            }
        }

    func fetchCrops() {
            guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
                errorMessage = "Authentication token is missing. Please log in again."
                isLoadingCrops = false
                return
            }

            APIClient.shared.fetchUserProfile(token: token) { result in
                switch result {
                case .success(let profile):
                    let userCrops = profile.crops
                    fetchCropImages(cropNames: userCrops)
                case .failure(let error):
                    errorMessage = "Failed to fetch user profile: \(error.localizedDescription)"
                    isLoadingCrops = false
                }
            }
        }

        // Fetch Images for Crops
        func fetchCropImages(cropNames: [String]) {
            let group = DispatchGroup()
            var fetchedCrops: [Crop] = []

            for cropName in cropNames {
                group.enter()
                APIClient.shared.getRandomImageURL(for: cropName) { result in
                    switch result {
                    case .success(let imageURL):
                        let harvestSeason = cropHarvestSeasons[cropName] ?? "Unknown"
                        fetchedCrops.append(Crop(name: cropName, imageName: imageURL, harvestSeason: harvestSeason))
                    case .failure:
                        let harvestSeason = cropHarvestSeasons[cropName] ?? "Unknown"
                        fetchedCrops.append(Crop(name: cropName, imageName: "placeholder", harvestSeason: harvestSeason))
                    }
                    group.leave()
                }
            }

            group.notify(queue: .main) {
                self.crops = fetchedCrops
                self.isLoadingCrops = false
            }
        }


    func fetchWeather() {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
            alertMessage = AlertItem(message: "Authentication token is missing. Please log in again.")
            return
        }

        APIClient.shared.fetchUserProfile(token: token) { result in
                    switch result {
                    case .success(let profile):
                        if let location = profile.location {
                            print("Using Profile Location: Latitude = \(location.latitude), Longitude = \(location.longitude)")
                            DispatchQueue.main.async {
                                self.userLocation = location // Save location for WeatherCardView
                            }
                            fetchWeatherData(latitude: location.latitude, longitude: location.longitude)
                        } else {
                            alertMessage = AlertItem(message: "User profile does not have a stored location.")
                        }
                    case .failure(let error):
                        alertMessage = AlertItem(message: "Failed to fetch user profile: \(error.localizedDescription)")
                    }
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
                DispatchQueue.main.async {
                    alertMessage = AlertItem(message: "Error fetching weather: \(error.localizedDescription)")
                }
            }
        }
    }
    
    private func fetchUserDataAndAnalyzeCropsForAlerts() {
            guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
                errorMessage = "User token not found. Please log in again."
                print("DEBUG: JWT token not found.")
                isLoading = false
                return
            }

            print("DEBUG: Using token: \(token)")

            APIClient.shared.fetchUserProfile(token: token) { result in
                switch result {
                case .success(let profile):
                    print("DEBUG: User profile fetched successfully: \(profile)")
                    fetchWeatherAndAnalyzeCrops(crops: profile.crops)
                case .failure(let error):
                    errorMessage = "Failed to fetch user profile: \(error.localizedDescription)"
                    print("DEBUG: Error fetching user profile: \(error)")
                    isLoading = false
                }
            }
        }

        /// Fetch weather data and analyze crops
        private func fetchWeatherAndAnalyzeCrops(crops: [String]) {
            guard let latitude = UserDefaults.standard.value(forKey: "USER_LATITUDE") as? Double,
                  let longitude = UserDefaults.standard.value(forKey: "USER_LONGITUDE") as? Double else {
                errorMessage = "Location not found. Please allow location access."
                print("DEBUG: Location not found in UserDefaults.")
                isLoading = false
                return
            }

            print("DEBUG: Using location - Latitude: \(latitude), Longitude: \(longitude)")

            WeatherAPI.shared.getWeather(latitude: latitude, longitude: longitude) { result in
                switch result {
                case .success(let weatherData):
                    print("DEBUG: Weather data fetched successfully: \(weatherData)")

                    let maxRain = weatherData.daily.precipitationSum.max() ?? 0.0
                    let minRain = weatherData.daily.precipitationSum.min() ?? 0.0
                    let maxTemp = weatherData.daily.temperatureMax.first ?? 0.0
                    let minTemp = weatherData.daily.temperatureMin.first ?? 0.0

                    print("DEBUG: Weather analysis parameters - Max Temp: \(maxTemp), Min Temp: \(minTemp), Max Rain: \(maxRain), Min Rain: \(minRain)")

                    let analysisRequest = WeatherAnalysisRequest(
                        maxTemp: maxTemp,
                        minTemp: minTemp,
                        maxRain: maxRain,
                        minRain: minRain,
                        cropNames: crops
                    )

                    APIClient.shared.getCropAnalysis(request: analysisRequest) { analysisResult in
                        switch analysisResult {
                        case .success(let cropAnalysisResponse):
                            print("DEBUG: Crop analysis response: \(cropAnalysisResponse)")

                            // Extract alerts from crop analysis response
                            let extractedAlerts = cropAnalysisResponse.cropAnalyses.values.flatMap { analysis in
                                analysis.alerts.map { alert in
                                    MyAlert(
                                        title: alert.title,
                                        reason: alert.message,
                                        description: "Severity: \(analysis.overallSeverity)",
                                        severity: analysis.overallSeverity
                                    )
                                }
                            }
                            DispatchQueue.main.async {
                                self.alerts = extractedAlerts
                                self.isLoading = false
                            }
                        case .failure(let error):
                            errorMessage = "Failed to fetch crop analysis: \(error.localizedDescription)"
                            print("DEBUG: Error fetching crop analysis: \(error)")
                            isLoading = false
                        }
                    }
                case .failure(let error):
                    errorMessage = "Failed to fetch weather data: \(error.localizedDescription)"
                    print("DEBUG: Error fetching weather data: \(error)")
                    isLoading = false
                }
            }
        }
}

struct WeatherCardView: View {
    let weatherData: WeatherResponse
    let location: UserLocation
    @State private var cityName: String = "Unknown City"

    var body: some View {
        ZStack {
            // Card Background with Image and Reduced Opacity
            Image("weather_background") // Replace with your image name
                .resizable()
                .scaledToFill()
                .opacity(0.4)
                .cornerRadius(16)
                .clipped()

            // Weather Details
            VStack(spacing: 12) { // Adjust spacing for better alignment
                // Weather Icon
                Image(determineWeatherIcon())
                    .resizable()
                    .scaledToFit()
                    .frame(width: 60, height: 60) // Adjust size for better alignment
                    .padding(.top, 10)

                // Dynamic City Name
                Text(cityName)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "#2c3e50"))

                // Weather Condition
                Text(determineWeatherCondition())
                    .font(.subheadline)
                    .foregroundColor(Color(hex: "#2c3e50"))

                // Temperature Range
                Text("\(weatherData.daily.temperatureMax[0], specifier: "%.1f")°C - \(weatherData.daily.temperatureMin[0], specifier: "%.1f")°C")
                    .font(.system(size: 24, weight: .bold, design: .rounded))
                    .foregroundColor(Color(hex: "#2c3e50"))
            }
            .padding(.horizontal)
            .padding(.bottom, 12) // Add padding to prevent elements from touching the bottom edge
        }
        .frame(height: 190) // Adjust card height if necessary
        .cornerRadius(16)
        .padding(.horizontal, 16)
        .onAppear {
            fetchCityName(latitude: location.latitude, longitude: location.longitude)
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


struct AlertCardView: View {
    let alert: MyAlert

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Top Band
            Color(alertSeverityColor())
                .frame(height: 8)
                .cornerRadius(4)
                .padding(.top, -16) // Adjust for proper positioning
            
            // Alert Icon and Title
            HStack {
                Image(systemName: "exclamationmark.triangle.fill")
                    .resizable()
                    .frame(width: 24, height: 24)
                    .foregroundColor(alertSeverityColor())
                Text(alert.title)
                    .font(.headline)
                    .foregroundColor(.red)
            }

            // Alert Reason (formatted with new lines at full stops)
            Text(formatText(alert.reason))
                .font(.subheadline)
                .foregroundColor(.gray)

            // Alert Description (formatted with new lines at full stops)
            Text(formatText(alert.description))
                .font(.footnote)
                .foregroundColor(.blue)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }

    /// Format text to add explicit newlines at full stops.
    private func formatText(_ text: String) -> String {
        text.replacingOccurrences(of: ". ", with: ".\n")
    }

    /// Determine the color based on the severity of the alert.
    private func alertSeverityColor() -> Color {
        switch alert.severity {
        case "HIGH":
            return .red
        case "MEDIUM":
            return .orange
        case "LOW":
            return .green
        default:
            return .gray
        }
    }
}

struct CropsCardView: View {
    let crop: Crop

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Use Kingfisher for image loading
            KFImage(URL(string: crop.imageName))
                .placeholder {
                    ProgressView() // Show a loading spinner while loading
                }
                .resizable()
                .scaledToFill()
                .frame(width: 100, height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                )

            // Crop Name
            Text(crop.name)
                .font(.headline)
                .foregroundColor(Color(hex: "#2c3e50"))

            // Harvest Season
            Text(crop.harvestSeason)
                .font(.subheadline)
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct Crop: Identifiable {
    let id = UUID()
    let name: String
    let imageName: String // URL or local image name
    let harvestSeason: String
}
