//
//  ContentView.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import SwiftUI

struct ContentView: View {
    @State private var isActive = false
    
    var body: some View {
        if isActive {
            MainView() // Replace with your main view
        } else {
            SplashView()
                .onAppear {
                    // Navigate to MainView after 4 seconds (animation duration)
                    DispatchQueue.main.asyncAfter(deadline: .now() + 4.0) {
                        isActive = true
                    }
                }
        }
    }
}


#Preview {
    ContentView()
}
