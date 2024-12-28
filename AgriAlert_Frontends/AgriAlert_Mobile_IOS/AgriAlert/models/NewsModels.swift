//
//  NewsModels.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import Foundation

struct NewsResponse: Decodable {
    let news: [NewsItem]
}

struct NewsItem: Identifiable, Decodable {
    var id: UUID = UUID()
    let title: String?
    let text: String?
    let image: String?
    let url: String?
    
    enum CodingKeys: String, CodingKey {
            case title, text, image, url
        }
}
