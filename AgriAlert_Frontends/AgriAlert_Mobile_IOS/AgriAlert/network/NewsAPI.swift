//
//  NewsAPI.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 24/12/2024.
//

import Foundation

class NewsAPI {
    static let shared = NewsAPI()
    private init() {} // Singleton pattern

    private let apiKey = "9f7f2f2a7052471f9fc6d6e478b38a77"
    private let baseURL = "https://api.worldnewsapi.com/search-news"

    func fetchNews(query: String, country: String, limit: Int, completion: @escaping (Result<[NewsItem], Error>) -> Void) {
        guard var urlComponents = URLComponents(string: baseURL) else {
            completion(.failure(NSError(domain: "Invalid URL", code: -1, userInfo: nil)))
            return
        }

        urlComponents.queryItems = [
            URLQueryItem(name: "text", value: query),
            URLQueryItem(name: "source-country", value: country),
            URLQueryItem(name: "number", value: "\(limit)"),
            URLQueryItem(name: "api-key", value: apiKey)
        ]

        guard let url = urlComponents.url else {
            completion(.failure(NSError(domain: "Invalid URL", code: -1, userInfo: nil)))
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }

            guard let data = data else {
                completion(.failure(NSError(domain: "No data received", code: -1, userInfo: nil)))
                return
            }

            do {
                let newsResponse = try JSONDecoder().decode(NewsResponse.self, from: data)
                completion(.success(newsResponse.news))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
}
