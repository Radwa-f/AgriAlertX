//
//  UserProfile.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import Foundation

// UserProfile Response Model
struct UserProfile: Decodable {
    let id: Int
    let firstName: String
    let lastName: String
    let email: String
    let phoneNumber: String
    let createdAt: String
    let lastLogin: String?
    let appUserRole: String
    let locked: Bool
    let enabled: Bool
    let location: UserLocation?
    let crops: [String]
    let credentialsNonExpired: Bool
    let authorities: [Authority]
    let username: String
    let accountNonExpired: Bool
    let accountNonLocked: Bool
}

// Nested Location Object
struct UserLocation: Decodable {
    let id: Int
    let latitude: Double
    let longitude: Double
}

// Nested Authority Object
struct Authority: Decodable {
    let authority: String
}

struct LocationUpdateRequest: Encodable {
    let latitude: Double
    let longitude: Double
}
struct AlertItem: Identifiable {
    let id = UUID()
    let message: String
}
