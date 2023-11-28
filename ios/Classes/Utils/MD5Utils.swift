//
//  MD5Utils.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import CommonCrypto

class MD5Utils {
    static func encryptStr() -> String {
        let passKey = "tachyphone2017"
        guard let dataBytes = passKey.data(using: .utf8) else {
            fatalError("Failed to convert passKey to data")
        }

        var resultBytes = [UInt8](repeating: 0, count: Int(CC_MD5_DIGEST_LENGTH))
        dataBytes.withUnsafeBytes { dataBytesPointer in
            guard let baseAddress = dataBytesPointer.baseAddress else {
                fatalError("Failed to get base address of dataBytes")
            }
            _ = CC_MD5(baseAddress, CC_LONG(dataBytes.count), &resultBytes)
        }

        let md5String = resultBytes.map { String(format: "%02hhx", $0) }.joined()
        return md5String
    }
}
