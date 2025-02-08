import SwiftUI
import MapKit

struct UpdateLocationView: View {
    let latitude: Double
    let longitude: Double

    @State private var selectedCoordinate = CLLocationCoordinate2D(latitude: 0, longitude: 0)
    @State private var searchQuery: String = ""
    @State private var isSaving = false
    @State private var alertItem: AlertItem? = nil

    var body: some View {
        NavigationView {
            VStack {
                // Search Bar
                SearchBar(text: $searchQuery, onSearch: { query in
                    searchLocation(query: query) // Perform the search
                })
                .padding(.horizontal)

                // Map View
                MapViewWithMarker(
                    initialCoordinate: CLLocationCoordinate2D(latitude: latitude, longitude: longitude),
                    selectedCoordinate: $selectedCoordinate
                )
                .frame(height: 500)
                .cornerRadius(12)
                .padding()

                Spacer()

                // Confirm Button
                Button(action: saveLocation) {
                    if isSaving {
                        ProgressView() // Show loading indicator while saving
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.green)
                            .cornerRadius(8)
                    } else {
                        Text("Confirm Location")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.green)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                }
                .disabled(isSaving)
                .padding()
            }
            .alert(item: $alertItem) { alert in
                            Alert(title: Text("Location Update"), message: Text(alert.message), dismissButton: .default(Text("OK")))
                        }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Text("Location Map")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(Color(hex: "#2c3e50"))
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func searchLocation(query: String) {
        let request = MKLocalSearch.Request()
        request.naturalLanguageQuery = query

        let search = MKLocalSearch(request: request)
        search.start { response, error in
            guard let coordinate = response?.mapItems.first?.placemark.coordinate, error == nil else {
                print("Location search failed: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            selectedCoordinate = coordinate
        }
    }

    private func saveLocation() {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else {
            alertItem = AlertItem(message: "Authentication token is missing. Please log in again.")
            return
        }

        isSaving = true
        let locationRequest = LocationUpdateRequest(latitude: selectedCoordinate.latitude, longitude: selectedCoordinate.longitude)

        APIClient.shared.updateLocation(token: token, locationRequest: locationRequest) { result in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success:
                                    alertItem = AlertItem(message: "Location updated successfully!")
                                case .failure(let error):
                                    alertItem = AlertItem(message: "Failed to update location: \(error.localizedDescription)")
                                }
            }
        }
    }
}



#Preview {
    UpdateLocationView(latitude: 1, longitude: 1)
}
