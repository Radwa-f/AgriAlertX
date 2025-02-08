import SwiftUI

struct LoginView: View {
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var errorMessage: String = ""
    @State private var isLoggedIn: Bool = false // Used to navigate to Home
    
    @StateObject private var locationManager = LocationManager() // Instantiate LocationManager

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Spacer()
                .frame(height: 100) // Add space to move the form down
            
            Text("Login")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Please fill out your credentials!")
                .foregroundColor(.gray)
            
            VStack(spacing: 15) {
                TextField("Email", text: $email)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                
                SecureField("Password", text: $password)
                    .padding(.vertical, 8)
                    .padding(.horizontal, 12)
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
            }
            
            // Button to trigger login
            Button(action: {
                login()
            }) {
                Text("Login")
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color(hex: "#3498db")) // Use the specific color
                    .cornerRadius(25) // Make edges more rounded
            }
            
            if !errorMessage.isEmpty {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }
            
            if locationManager.permissionDenied {
                Text("Location permission denied. Please enable it in Settings.")
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }
            
            Spacer()
            
            Text("Swipe left for Register")
                .font(.footnote)
                .foregroundColor(.gray)
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .padding()
        .fullScreenCover(isPresented: $isLoggedIn) {
            HomeView() // Navigate to the Home screen
        }
        .onAppear {
            locationManager.requestLocation() // Request location access when the view appears
        }
    }
    
    func login() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please fill all fields"
            return
        }
        
        guard let latitude = locationManager.latitude, let longitude = locationManager.longitude else {
            errorMessage = "Location not available. Please enable location services."
            return
        }
        
        print("User's Location: Latitude = \(latitude), Longitude = \(longitude)")
        
        // Save location in UserDefaults
        UserDefaults.standard.set(latitude, forKey: "USER_LATITUDE")
        UserDefaults.standard.set(longitude, forKey: "USER_LONGITUDE")
        
        APIClient.shared.login(email: email, password: password) { result in
            switch result {
            case .success(let token):
                print("Login successful! Token: \(token)")
                UserDefaults.standard.set(token, forKey: "JWT_TOKEN")
                KeychainHelper.shared.save(key: "JWT_TOKEN", value: token) 
                isLoggedIn = true // Navigate to Home screen
            case .failure(let error):
                errorMessage = "Login failed: \(error.localizedDescription)"
            }
        }
    }

}
