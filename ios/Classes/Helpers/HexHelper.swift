//
//  HexHelper.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

class HexHelper {
    private let TAG = "HexHelper"
    private let stringHelper = StringHelper()

    func byteArrayToHexString(buffer: [UInt8]) -> String {
        var bufferString = ""
        for byte in buffer {
            let hexChar = String(format: "%02X", byte)
            bufferString += hexChar + " "
        }
        return bufferString.trimmingCharacters(in: .whitespaces)
    }

    func hexStringToByteArray(hexString: String) -> [UInt8] {
        let sanitized = hexString.replacingOccurrences(of: " ", with: "")
        let length = sanitized.count
        var byteArray = [UInt8]()

        for i in stride(from: 0, to: length, by: 2) {
            let startIndex = sanitized.index(sanitized.startIndex, offsetBy: i)
            let endIndex = sanitized.index(startIndex, offsetBy: 2)
            let byteString = sanitized[startIndex..<endIndex]
            if let byte = UInt8(byteString, radix: 16) {
                byteArray.append(byte)
            }
        }

        return byteArray
    }

    func cleanupHexString(hexString: String) -> String {
        return stringHelper.removeWhitespaces(input: hexString)
    }

    func byteLength(apdu: ApduCommand? = nil, length: Int = 0) -> String {
        if let apdu = apdu, apdu.lengthMin == apdu.lengthMax, apdu.lengthMax <= 255 {
            return padHex(hexString: String(apdu.lengthMin, radix: 16), desiredLength: 2).uppercased()
        } else {
            return padHex(hexString: String(length, radix: 16), desiredLength: 2).uppercased()
        }
    }

    func calculateLengthOfHex(hexString: String) -> String {
        let length = cleanupHexString(hexString: hexString).count / 2
        return padHex(hexString: String(format: "%04X", length), desiredLength: 4).uppercased()
    }

    func calculateLengthToHex(length: Int) -> String {
        return padHex(hexString: String(format: "%04X", length), desiredLength: 4).uppercased()
    }

    func padHex(hexString: String, desiredLength: Int = 2) -> String {
        let paddedHex = String(repeating: "0", count: max(0, desiredLength - hexString.count)) + hexString

        let spacedHex = stride(from: 0, to: paddedHex.count, by: 2).map {
            paddedHex[paddedHex.index(paddedHex.startIndex, offsetBy: $0)..<paddedHex.index(paddedHex.startIndex, offsetBy: $0 + 2)]
        }.joined(separator: " ")

        return spacedHex.uppercased()
    }
}

