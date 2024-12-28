//
//  RegistrationRequest.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

//
//  RegistrationRequest.swift
//  AgriAlert
//

import Foundation

struct RegistrationRequest: Encodable {
    let firstName: String
    let lastName: String
    let email: String
    let password: String
    let phoneNumber: String
    let location: Location
    let crops: [String]
}

struct Location: Encodable {
    let latitude: Double
    let longitude: Double
}
