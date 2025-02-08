//
//  KeychainHelper.swift
//  AgriAlert
//
//  Created by Fattouhi Radwa on 4/1/2025.
//

import Security
import Foundation

class KeychainHelper {
    static let shared = KeychainHelper()
    

        private init() {}

        private let tokenKey = "JWT_TOKEN"

        /// Save the token securely in Keychain
        func saveToken(_ token: String) {
            save(key: tokenKey, value: token)
        }

        /// Retrieve the token from Keychain
        func getToken() -> String? {
            return retrieve(key: tokenKey)
        }

        /// Delete the token from Keychain
        func deleteToken() {
            delete(key: tokenKey)
        }

        /// Decode a JWT token to extract the payload
        private func decodeJWT(_ token: String) -> [String: Any]? {
            let segments = token.split(separator: ".")
            guard segments.count == 3 else { return nil }

            let payloadSegment = segments[1]
            guard let payloadData = Data(base64Encoded: String(payloadSegment).paddedBase64),
                  let json = try? JSONSerialization.jsonObject(with: payloadData, options: []),
                  let payload = json as? [String: Any] else {
                return nil
            }

            return payload
        }

        /// Check if the token is valid or expired
        func isTokenValid() -> Bool {
            guard let token = getToken(),
                  let payload = decodeJWT(token),
                  let exp = payload["exp"] as? Double else {
                return false // Token doesn't exist or is invalid
            }

            let expirationDate = Date(timeIntervalSince1970: exp)
            return Date() < expirationDate
        }

        /// Refresh the token if needed (requires backend support)
        func refreshTokenIfNeeded(completion: @escaping (Bool) -> Void) {
            if isTokenValid() {
                completion(true)
                return
            }

            //next sprint 
            
        }

    func save(key: String, value: String) {
        let data = value.data(using: .utf8)!
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        SecItemDelete(query as CFDictionary) // Delete existing item if any
        SecItemAdd(query as CFDictionary, nil)
    }

    func retrieve(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}

private extension String {
    /// Add padding to a Base64 string to ensure it's valid
    var paddedBase64: String {
        self.padding(toLength: ((self.count + 3) / 4) * 4, withPad: "=", startingAt: 0)
    }
}
