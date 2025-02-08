//
//  SplashView.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import SwiftUI

struct SplashView: View {
    @State private var scale: CGFloat = 1.0
    @State private var offsetY: CGFloat = 0.0
    @State private var opacity: Double = 1.0
    
    var body: some View {
        ZStack {
            Color.white
                .ignoresSafeArea()
            
            Image("logo") // Use the same asset name as your logo
                .resizable()
                .scaledToFit()
                .frame(width: 700, height: 600) // Adjust size as needed
                .scaleEffect(scale)
                .offset(y: offsetY)
                .opacity(opacity)
                .onAppear {
                    animateLogo()
                }
        }
    }
    
    func animateLogo() {
        // Step 1: Scale down
        withAnimation(.easeInOut(duration: 2.0)) {
            scale = 0.7
        }
        // Step 2: Translate upward
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            withAnimation(.easeInOut(duration: 1.0)) {
                offsetY = -100
            }
        }
        // Step 3: Fade out
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
            withAnimation(.easeInOut(duration: 4.0)) {
                opacity = 0
            }
        }
    }
}

#Preview {
    SplashView()
}
