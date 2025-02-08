import SwiftUI
import Alamofire
import Kingfisher

struct CropsTabView: View {
    @State private var cropAnalysisList: [CropAnalysisItem] = []
    @State private var userCrops: [String] = []
    @State private var isLoading: Bool = true
    @State private var errorMessage: String = ""

    var body: some View {
        VStack {
            if isLoading {
                ProgressView("Loading Crop Analysis...")
            } else if !errorMessage.isEmpty {
                Text(errorMessage)
                    .foregroundColor(.red)
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        ForEach(cropAnalysisList) { crop in
                            CropCardView(crop: crop)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .onAppear {
            fetchUserDataAndAnalyzeCrops()
        }
    }

    /// Fetch user data, crop list, and analyze crops
    private func fetchUserDataAndAnalyzeCrops() {
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
                self.userCrops = profile.crops
                print("DEBUG: User crops: \(self.userCrops)")
                fetchWeatherAndAnalyzeCrops(crops: userCrops)
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
                        
                        // Create a dispatch group to handle multiple async image fetches
                        let group = DispatchGroup()
                        var cropItems: [CropAnalysisItem] = []
                        
                        for (key, value) in cropAnalysisResponse.cropAnalyses {
                            group.enter()
                            
                            // Fetch image for each crop
                            APIClient.shared.getRandomImageURL(for: key) { result in
                                switch result {
                                case .success(let imageURL):
                                    let item = CropAnalysisItem(
                                        cropName: key,
                                        analysis: value,
                                        imageURL: imageURL
                                    )
                                    cropItems.append(item)
                                case .failure(let error):
                                    print("DEBUG: Failed to fetch image for \(key): \(error)")
                                    let item = CropAnalysisItem(
                                        cropName: key,
                                        analysis: value,
                                        imageURL: "https://via.placeholder.com/600x400?text=Image+Unavailable"
                                    )
                                    cropItems.append(item)
                                }
                                group.leave()
                            }
                        }
                        
                        group.notify(queue: .main) {
                            self.cropAnalysisList = cropItems
                            isLoading = false
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

struct CropAnalysisItem: Identifiable {
    let id = UUID()
    let cropName: String
    let analysis: CropAnalysis
    let imageURL: String
}

struct CropCardView: View {
    let crop: CropAnalysisItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Use Kingfisher for image loading
            KFImage(URL(string: crop.imageURL))
                .placeholder {
                    ProgressView() // Show a loading spinner instead of gray box
                }
                .onSuccess { result in
                    print("DEBUG: Successfully fetched image for \(crop.cropName)")
                }
                .onFailure { error in
                    print("DEBUG: Failed to fetch image for \(crop.cropName): \(error.localizedDescription)")
                }
                .resizable()
                .aspectRatio(contentMode: .fill) // Use aspectRatio instead of scaledToFill
                .frame(height: 200)
                .clipped() // Add clipped() to prevent image overflow
                .cornerRadius(12)

            // Crop Name
            Text(crop.cropName)
                .font(.title3)
                .fontWeight(.bold)

            // Overall Severity
            Text("Overall Severity: \(crop.analysis.overallSeverity)")
                .font(.subheadline)
                .foregroundColor(crop.analysis.overallSeverity == "HIGH" ? .red : .green)

            // Alerts
            Text("Alerts")
                .font(.headline)
                .padding(.top, 4)

            Text(crop.analysis.alerts.map { "\($0.title): \($0.message)" }.joined(separator: "\n"))
                .font(.footnote)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
}

