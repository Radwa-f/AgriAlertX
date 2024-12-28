import SwiftUI
import CoreLocation

struct RegisterView: View {
    @State private var firstName: String = ""
    @State private var lastName: String = ""
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var confirmPassword: String = ""
    @State private var phone: String = ""
    
    @State private var crops: [String] = [
        "Rice", "Wheat", "Maize", "Millets", "Bajra (Pearl Millet)",
        "Pulses (Kharif)", "Lentil (Rabi)", "Oilseeds", "Groundnut",
        "Sugarcane", "Sugar beet", "Cotton", "Tea", "Coffee", "Cocoa",
        "Rubber", "Jute", "Flax", "Coconut", "Oil-palm", "Clove",
        "Black Pepper", "Cardamom", "Turmeric"
    ]
    @State private var selectedCrops: [String] = []
    @State private var searchText: String = ""
    @State private var isDropdownOpen: Bool = false

    @State private var latitude: Double? = nil
    @State private var longitude: Double? = nil
    @State private var errorMessage: String = ""
    @State private var showAlert: Bool = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Spacer()
                    .frame(height: 0)
                
                Text("Register")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text("Please fill out your details!")
                    .foregroundColor(.gray)
                
                // Input fields
                TextField("First Name", text: $firstName)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                
                TextField("Last Name", text: $lastName)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                
                TextField("Email", text: $email)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                
                SecureField("Password", text: $password)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                
                SecureField("Confirm Password", text: $confirmPassword)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                
                TextField("Phone", text: $phone)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                    .keyboardType(.phonePad)
                
                // Crop Selection Field
                VStack(alignment: .leading) {
                    ZStack(alignment: .leading) {
                        if selectedCrops.isEmpty && searchText.isEmpty {
                            Text("Select crops...")
                                .foregroundColor(.gray)
                                .padding(.leading, 10)
                        }
                        
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 5) {
                                ForEach(selectedCrops, id: \.self) { crop in
                                    HStack {
                                        Text(crop)
                                            .padding(.vertical, 5)
                                            .padding(.horizontal, 10)
                                            .background(Color(hex: "#3498db"))
                                            .foregroundColor(.white)
                                            .cornerRadius(15)
                                        
                                        Button(action: {
                                            selectedCrops.removeAll { $0 == crop }
                                        }) {
                                            Image(systemName: "xmark.circle.fill")
                                                .foregroundColor(.white)
                                        }
                                    }
                                }
                                
                                TextField("Select crops...", text: $searchText, onEditingChanged: { editing in
                                    isDropdownOpen = editing
                                })
                                .frame(minWidth: 100)
                                .padding(.vertical, 5)
                                .padding(.horizontal, 10)
                                .background(Color(.systemGray6))
                                .cornerRadius(15)
                            }
                        }
                    }
                    .padding(.vertical, 8)
                    .background(Color(.systemGray6))
                    .cornerRadius(6)
                    
                    if isDropdownOpen {
                        List {
                            ForEach(crops.filter { searchText.isEmpty ? true : $0.localizedCaseInsensitiveContains(searchText) }, id: \.self) { crop in
                                Button(action: {
                                    if !selectedCrops.contains(crop) {
                                        selectedCrops.append(crop)
                                    }
                                    searchText = ""
                                    isDropdownOpen = false
                                }) {
                                    Text(crop)
                                }
                            }
                        }
                        .frame(height: 200)
                    }
                }
                
                Button(action: {
                    register()
                }) {
                    Text("Register")
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(hex: "#3498db"))
                        .cornerRadius(25)
                }
                
                if !errorMessage.isEmpty {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                }
                
                Spacer()
            }
            .padding()
        }
        .onAppear {
            fetchLocation()
        }
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text("Registration Successful"),
                message: Text("Your details have been saved successfully."),
                dismissButton: .default(Text("OK"))
            )
        }
    }

    func fetchLocation() {
        let manager = CLLocationManager()
        manager.requestWhenInUseAuthorization()
        
        if let location = manager.location {
            latitude = location.coordinate.latitude
            longitude = location.coordinate.longitude
        } else {
            errorMessage = "Unable to fetch location. Please enable GPS."
        }
    }

    func register() {
        guard !firstName.isEmpty, !lastName.isEmpty, !email.isEmpty, !password.isEmpty, !phone.isEmpty else {
            errorMessage = "Please fill all fields"
            return
        }
        
        guard password == confirmPassword else {
            errorMessage = "Passwords do not match"
            return
        }
        
        guard let lat = latitude, let lon = longitude else {
            errorMessage = "Location not available"
            return
        }
        
        let location = Location(latitude: lat, longitude: lon)
        let request = RegistrationRequest(
            firstName: firstName,
            lastName: lastName,
            email: email,
            password: password,
            phoneNumber: phone,
            location: location,
            crops: selectedCrops
        )
        
        APIClient.shared.registerUser(request: request) { result in
            switch result {
            case .success:
                clearInputs()
                showAlert = true
            case .failure(let error):
                errorMessage = "Registration failed: \(error.localizedDescription)"
            }
        }
    }

    func clearInputs() {
        firstName = ""
        lastName = ""
        email = ""
        password = ""
        confirmPassword = ""
        phone = ""
        selectedCrops.removeAll()
        searchText = ""
    }
}

#Preview {
    RegisterView()
}
