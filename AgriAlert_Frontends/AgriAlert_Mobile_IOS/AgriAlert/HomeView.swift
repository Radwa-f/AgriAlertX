import SwiftUI
import CoreLocation

struct HomeView: View {

    init() {
        setupTabBarAppearance()
    }

    var body: some View {
        NavigationView {
            VStack {
                // Top Toolbar
                HStack {
                                    // Left Icon
                                    NavigationLink(destination: ProfileView()) {
                                        Image(systemName: "person.circle.fill")
                                            .font(.system(size: 50))
                                            .foregroundColor(Color(hex: "#264d50"))
                                            .padding()
                                    }

                                    Spacer()

                                    // Title
                                    Text("AgriAlertX")
                                        .font(.system(size: 30, weight: .bold)) // Increased font size
                                        .foregroundColor(Color(hex: "#2c3e50")) // Darker title color

                                    Spacer()

                                    // Right Icon
                    NavigationLink(destination: ChatBotView()) {
                        Image(systemName: "message.circle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(Color(hex: "#264d50"))
                            .padding()
                    }

                                }
                                .padding(.top, 0)

                // Bottom Navigation Bar
                TabView {
                    HomeTabView()
                        .tabItem {
                            Label("Home", systemImage: "house.fill")
                        }

                    CropsTabView()
                        .tabItem {
                            Label("Crops", systemImage: "leaf.fill")
                        }

                    NewsTabView()
                        .tabItem {
                            Label("News", systemImage: "newspaper.fill")
                        }

                    SettingsTabView()
                        .tabItem {
                            Label("Settings", systemImage: "gearshape.fill")
                        }
                }            }
        }
    }

    private func setupTabBarAppearance() {
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor.white
        appearance.stackedLayoutAppearance.normal.iconColor = UIColor(Color(hex: "#264d50"))
        appearance.stackedLayoutAppearance.selected.iconColor = UIColor(Color(hex: "#264d50"))
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor(Color(hex: "#264d50"))]
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor(Color(hex: "#264d50"))]

        UITabBar.appearance().standardAppearance = appearance
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = appearance
        }
    }
}

#Preview {
    HomeView()
}
