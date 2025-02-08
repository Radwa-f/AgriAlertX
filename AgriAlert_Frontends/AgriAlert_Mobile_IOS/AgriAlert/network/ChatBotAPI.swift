//
//  ChatBotAPI.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 3/1/2025.
//

import Alamofire
import Foundation



class ChatBotAPI {
    static let shared = ChatBotAPI()
    private init() {}

    func getChatResponse(message: String, completion: @escaping (Result<String, Error>) -> Void) {
        let url = "http://127.0.0.1:5000/chat" // Update with your server address
        let parameters = ["query": message]

        print("DEBUG: Sending message to chatbot: \(message)")

        AF.request(url, method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .validate()
            .responseDecodable(of: ChatResponse.self) { response in
                switch response.result {
                case .success(let data):
                    print("DEBUG: Chatbot API response: \(data.response)")
                    completion(.success(data.response))
                case .failure(let error):
                    if let data = response.data, let rawResponse = String(data: data, encoding: .utf8) {
                        print("ERROR: Chatbot API response error: \(rawResponse)")
                    }
                    print("ERROR: Chatbot API call failed: \(error.localizedDescription)")
                    completion(.failure(error))
                }
            }
    }
}
