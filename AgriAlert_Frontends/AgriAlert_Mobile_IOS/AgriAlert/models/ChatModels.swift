//
//  ChatModels.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 3/1/2025.
//
import Foundation

struct ChatRequest: Codable {
    let query: String
}

struct ChatResponse: Codable {
    let response: String
}


struct ChatMessage: Identifiable {
    let id = UUID()
    let sender: String // "User" or "Bot"
    let content: String
}

struct ChatError: Identifiable {
    let id = UUID()
    let message: String
}
