//
//  AESUtils.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import UIKit
import CryptoSwift

extension String {

    func encrypt(key: String) throws -> String {
        var result = ""
        do {
            let key: [UInt8] = Array(key.utf8) as [UInt8]
            let aes = try! AES(key: key, blockMode: ECB(), padding: .pkcs5) // AES128 .ECB pkcs7
            let encrypted = try aes.encrypt(Array(self.utf8))
            result = encrypted.toBase64()
        } catch {
            print("Error: \(error)")
            throw error
        }
        print("AES Encryption Result: \(result)")
        return result
    }

    func decrypt(key: String) throws -> String {
        var result = ""
        do {
            let encrypted = self
            let key: [UInt8] = Array(key.utf8) as [UInt8]
            let aes = try! AES(key: key, blockMode: ECB(), padding: .pkcs5) // AES128 .ECB pkcs7
            let decrypted = try aes.decrypt(Array(base64: encrypted))
            result = String(data: Data(decrypted), encoding: .utf8) ?? ""
        } catch {
            print("Error: \(error)")
            throw error
        }
        print("AES Decryption Result: \(result)")
        return result
    }
}
