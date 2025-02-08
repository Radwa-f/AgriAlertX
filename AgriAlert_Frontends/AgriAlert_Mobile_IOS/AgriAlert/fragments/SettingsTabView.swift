import SwiftUI

struct SettingsTabView: View {
    @State private var isWeatherNotificationsEnabled = true
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @Environment(\.presentationMode) var presentationMode
        @State private var navigateToLogin: Bool = false
        @State private var isDeletingAccount: Bool = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // User Preferences Section
                Text("User Preferences")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)
                
                NavigationLink(destination: ProfileView()) {
                    SettingsRow(icon: "person.circle.fill", text: "Edit Profile")
                }
                
                // Notifications Section
                Text("Notifications")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)
                
                Toggle("Weather Updates", isOn: $isWeatherNotificationsEnabled)
                    .padding()
                    .background(Color.white)
                    .cornerRadius(12)
                    .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
                    .padding(.horizontal)
                    .onChange(of: isWeatherNotificationsEnabled) { value in
                        toggleWeatherNotifications(value)
                    }
                
                // Help Center Section
                Text("Help Center")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)
                
                NavigationLink(destination: HelpCenterView()) {
                    SettingsRow(icon: "questionmark.circle.fill", text: "Need Help?")
                }
                
                // Manage Session Section
                Text("Manage Session")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)
                
                Button(action: {
                    handleLogout()
                }) {
                    Text("Logout")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(hex: "#3498db"))
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .bold()
                }
                .padding(.horizontal)
                
                Button(action: {
                    deleteAccount()
                }) {
                    Text("Delete Account")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .bold()
                }
                .padding(.horizontal)
            }
            .padding(.top, 16)
        }
        .fullScreenCover(isPresented: $navigateToLogin) {
                MainView()
            }
        .navigationTitle("Settings")

    }
    
    // Toggle Weather Notifications
    private func toggleWeatherNotifications(_ isEnabled: Bool) {
        let message = isEnabled ? "Weather Notifications Enabled" : "Weather Notifications Disabled"
        print(message)
        saveNotificationPreference(isEnabled)
    }
    
    // Save Notification Preference
    private func saveNotificationPreference(_ isEnabled: Bool) {
        UserDefaults.standard.set(isEnabled, forKey: "weather_notifications")
        print("DEBUG: Notification preference saved: \(isEnabled)")
    }
    
    // Logout Logic
    func handleLogout() {
            // Clear user session
        KeychainHelper.shared.deleteToken()
            UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
            UserDefaults.standard.synchronize()

            // Navigate to LoginView
            navigateToLogin = true
        }
    
    // Delete Account Logic
    private func deleteAccount() {
        isLoading = true
        // Call API to delete the account
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isLoading = false
            print("DEBUG: Account deleted")
            UserDefaults.standard.removeObject(forKey: "JWT_TOKEN")
            // Navigate to login view
        }
    }
}

struct SettingsRow: View {
    let icon: String
    let text: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(Color(hex: "#2c3e50"))
            Text(text)
                .foregroundColor(Color(hex: "#2c3e50"))
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
        .padding(.horizontal)
    }
}

import SwiftUI

struct HelpCenterView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Title
                HStack {
                    Spacer()
                    Text("Help Center")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: "#2c3e50"))
                    Spacer()
                }
                .padding(.top)

                // FAQ Section
                Text("Frequently Asked Questions (FAQ)")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)

                VStack(alignment: .leading, spacing: 8) {
                    Text("• How do I reset my password?")
                        .font(.body)
                        .foregroundColor(.gray)

                    Text("• How can I contact support?")
                        .font(.body)
                        .foregroundColor(.gray)

                    Text("• How do I update my profile information?")
                        .font(.body)
                        .foregroundColor(.gray)
                }
                .padding(.horizontal)

                // Meet the Team Section
                Text("Meet the Team")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)

                VStack(spacing: 16) {
                    TeamMemberCard(
                        name: "Radwa Fattouhi",
                        role: "Lead Developer",
                        description: "Radwa, an engineering student at the National School of Applied Sciences of El Jadida, is the lead developer of Agrialert, where she oversees software efficiency.",
                        backgroundColor: Color.pink
                    )

                    TeamMemberCard(
                        name: "Saifeddine Douidy",
                        role: "Lead Developer",
                        description: "Saifeddine, an engineering student at the National School of Applied Sciences of El Jadida, is the lead developer of Agrialert, shaping its architecture and coding.",
                        backgroundColor: Color.blue
                    )
                }
                .padding(.horizontal)

                // Contact Information Section
                Text("Contact Information")
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2c3e50"))
                    .padding(.horizontal)

                VStack(alignment: .leading, spacing: 8) {
                    Text("Email: agrialert2024@gmail.com")
                        .font(.body)
                        .foregroundColor(.gray)

                    Text("Phone: +212655593397")
                        .font(.body)
                        .foregroundColor(.gray)
                }
                .padding(.horizontal)
            }
            .padding(.bottom, 16) // Add padding at the bottom for spacing
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct TeamMemberCard: View {
    let name: String
    let role: String
    let description: String
    let backgroundColor: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Name Header
            Text(name)
                .font(.headline)
                .foregroundColor(.white)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(backgroundColor)
                .cornerRadius(12, corners: [.topLeft, .topRight])

            // Role and Description
            VStack(alignment: .leading, spacing: 8) {
                Text(role)
                    .font(.subheadline)
                    .foregroundColor(Color(hex: "#2c3e50"))

                Text(description)
                    .font(.footnote)
                    .foregroundColor(.gray)
            }
            .padding()
        }
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

// Helper function for custom corner radius
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = 0
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}


#Preview {
    NavigationView {
        SettingsTabView()
    }
}
