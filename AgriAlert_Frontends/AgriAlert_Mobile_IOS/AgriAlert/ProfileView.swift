import SwiftUI
import CoreLocation

struct ProfileView: View {
    @State private var userProfile: UserProfile?
    @State private var errorMessage: String = ""
    @State private var isLoading: Bool = true
    @State private var cityName: String = "Unknown City"
    @State private var navigateToProfileUpdate: Bool = false
    @State private var navigateToLocationUpdate: Bool = false
    @State private var locationLatitude: Double = 0.0
    @State private var locationLongitude: Double = 0.0

    var body: some View {
        VStack(spacing: 16) {
            if isLoading {
                ProgressView("Loading Profile...")
            } else if let profile = userProfile {
                ScrollView {
                    VStack(spacing: 16) {
                        // Farmer Placeholder Image
                        Image("ic_farmer")
                            .resizable()
                            .scaledToFit()
                            .frame(height: 150)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                            .padding(.top, 16)

                        // Cards Container
                        VStack(spacing: 16) {
                            // User Info Card
                            CardViewWithIcon(title: "User Info", action: {
                                // Navigate to UpdateProfileView
                                navigateToUpdateProfileView()
                            }) {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("Name: \(profile.firstName) \(profile.lastName)")
                                    Text("Email: \(profile.email)")
                                    Text("Phone: \(profile.phoneNumber)")
                                }
                            }

                            // Location Card
                            CardViewWithIcon(title: "Location", action: {
                                // Navigate to UpdateLocationView
                                navigateToUpdateLocationView(latitude: profile.location?.latitude ?? 0.0,
                                                             longitude: profile.location?.longitude ?? 0.0)
                            }) {
                                VStack(alignment: .leading, spacing: 8) {
                                    if let location = profile.location {
                                        Text("Latitude: \(location.latitude)")
                                        Text("Longitude: \(location.longitude)")
                                        Text("City: \(cityName)")
                                            .onAppear {
                                                fetchCityName(latitude: location.latitude, longitude: location.longitude)
                                            }
                                    } else {
                                        Text("Location not available.")
                                    }
                                }
                            }

                            CardView(title: "Crops", iconName: "leaf") {
                                VStack(alignment: .leading, spacing: 8) {
                                    if profile.crops.isEmpty {
                                        Text("No crops assigned.")
                                    } else {
                                        Text(profile.crops.joined(separator: ", "))
                                            .lineLimit(nil) // Allows the text to wrap if it's too long
                                    }
                                }
                            }


                            
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 16)
                    }
                }
            } else {
                Text(errorMessage)
                    .foregroundColor(.red)
            }
        }
        .toolbar {
            ToolbarItem(placement: .principal) { // Centered title
                Text("User Profile")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(Color(hex: "#2c3e50"))
            }
        }
        .onAppear(perform: fetchUserProfile)
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.inline)
        .background(
                        NavigationLink(
                            destination: UpdateProfileView(),
                            isActive: $navigateToProfileUpdate
                        ) { EmptyView() }
                    )
                    .background(
                        NavigationLink(
                            destination: UpdateLocationView(latitude: locationLatitude, longitude: locationLongitude),
                            isActive: $navigateToLocationUpdate
                        ) { EmptyView() }
                    )
    }

    private func fetchUserProfile() {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
            errorMessage = "Authentication token is missing. Please log in again."
            isLoading = false
            return
        }

        APIClient.shared.fetchUserProfile(token: token) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let profile):
                    self.userProfile = profile
                case .failure(let error):
                    self.errorMessage = "Failed to fetch profile: \(error.localizedDescription)"
                }
            }
        }
    }

    private func fetchCityName(latitude: Double, longitude: Double) {
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

    private func navigateToUpdateProfileView() {
            navigateToProfileUpdate = true
        }

        private func navigateToUpdateLocationView(latitude: Double, longitude: Double) {
            locationLatitude = latitude
            locationLongitude = longitude
            navigateToLocationUpdate = true
        }
}

// Reusable CardView Component
struct CardView<Content: View>: View {
    let title: String
    let iconName: String? // Optional icon name
    let content: Content

    init(title: String, iconName: String? = nil, @ViewBuilder content: () -> Content) {
        self.title = title
        self.iconName = iconName
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                Spacer()
                if let iconName = iconName {
                    Image(systemName: iconName)
                        .foregroundColor(.blue) // Adjust color if needed
                }
            }
            content
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
}


// CardView with an Update Icon
struct CardViewWithIcon<Content: View>: View {
    let title: String
    let action: () -> Void
    let content: Content

    init(title: String, action: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.title = title
        self.action = action
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                Spacer()
                Button(action: action) {
                    Image(systemName: "pencil")
                        .foregroundColor(.blue)
                }
            }
            content
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
}
