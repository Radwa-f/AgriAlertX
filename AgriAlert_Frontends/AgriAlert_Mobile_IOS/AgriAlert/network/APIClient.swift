//
//  APIClient.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 23/12/2024.
//

import Alamofire

class APIClient {
    static let shared = APIClient() // Singleton
    private init() {}

    private let baseURL = "http://localhost:8087/" // Replace with your actual base URL

    func login(email: String, password: String, completion: @escaping (Result<String, Error>) -> Void) {
        let url = "\(baseURL)api/v1/login"
        let parameters = LoginRequest(email: email, password: password)

        AF.request(url, method: .post, parameters: parameters, encoder: JSONParameterEncoder.default)
            .validate() // Validate status code
            .responseString { response in
                switch response.result {
                case .success(let token):
                    completion(.success(token)) // Return the token
                case .failure(let error):
                    completion(.failure(error)) // Return the error
                }
            }
    }
    func registerUser(request: RegistrationRequest, completion: @escaping (Result<Void, Error>) -> Void) {
            let url = "\(baseURL)api/v1/registration"
            
            AF.request(url, method: .post, parameters: request, encoder: JSONParameterEncoder.default)
                .validate() // Automatically validates the response status code
                .response { response in
                    switch response.result {
                    case .success:
                        completion(.success(()))
                    case .failure(let error):
                        completion(.failure(error))
                    }
                }
        }
    
    func fetchUserProfile(token: String, completion: @escaping (Result<UserProfile, Error>) -> Void) {
            let url = "\(baseURL)api/v1/user/profile"
            let headers: HTTPHeaders = ["Authorization": "Bearer \(token)"]
            
            AF.request(url, method: .get, headers: headers)
                .validate()
                .responseDecodable(of: UserProfile.self) { response in
                    switch response.result {
                    case .success(let profile):
                        completion(.success(profile))
                    case .failure(let error):
                        completion(.failure(error))
                    }
                }
        }
        
        func getCropAnalysis(request: WeatherAnalysisRequest, completion: @escaping (Result<CropAnalysisResponse, Error>) -> Void) {
            let url = "\(baseURL)api/crops/weather-analysis"
            
            AF.request(url, method: .post, parameters: request, encoder: JSONParameterEncoder.default)
                .validate()
                .responseDecodable(of: CropAnalysisResponse.self) { response in
                    switch response.result {
                    case .success(let cropAnalysis):
                        completion(.success(cropAnalysis))
                    case .failure(let error):
                        completion(.failure(error))
                    }
                }
        }
    
    func getRandomImageURL(for query: String, completion: @escaping (Result<String, Error>) -> Void) {
        let url = "https://api.unsplash.com/photos/random"
        let parameters: [String: Any] = [
            "client_id": "SS9TfnNCVbvw9318k1VjoWy7je20hdrVy3_2BlgFmFE",
            "query": query
        ]
        
        AF.request(url, method: .get, parameters: parameters)
            .validate()
            .responseDecodable(of: UnsplashPhotoResponse.self) { response in
                switch response.result {
                case .success(let data):
                    print("DEBUG: Unsplash image URL fetched: \(data.urls.regular)")
                    completion(.success(data.urls.regular))
                case .failure(let error):
                    print("DEBUG: Error fetching Unsplash image: \(error.localizedDescription)")
                    completion(.failure(error))
                }
            }
    }


}
