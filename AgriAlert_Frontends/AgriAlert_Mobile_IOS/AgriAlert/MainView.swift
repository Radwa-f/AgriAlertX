//
//  MainView.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import SwiftUI

struct MainView: View {
    @State private var selectedTab: String = "Login" // Current tab
    
    var body: some View {
        VStack(spacing: 0) {
            // Custom navigation bar
            HStack {
                Button(action: {
                    selectedTab = "Login"
                }) {
                    Text("LOGIN")
                        .foregroundColor(selectedTab == "Login" ? Color(hex: "#3498db") : .gray)
                        .fontWeight(selectedTab == "Login" ? .bold : .regular)
                }
                .frame(maxWidth: .infinity)
                
                Button(action: {
                    selectedTab = "Register"
                }) {
                    Text("REGISTER")
                        .foregroundColor(selectedTab == "Register" ? Color(hex: "#3498db") : .gray)
                        .fontWeight(selectedTab == "Register" ? .bold : .regular)
                }
                .frame(maxWidth: .infinity)
            }
            .padding()
            .background(Color.white)
            
            Divider()
            
            // Display content based on the selected tab
            if selectedTab == "Login" {
                LoginView()
            } else {
                RegisterView()
            }
        }
    }
}


#Preview {
    MainView()
}
