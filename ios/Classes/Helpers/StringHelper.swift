//
//  StringHelper.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

class StringHelper {
    func removeWhitespaces(input: String) -> String {
        return input.replacingOccurrences(of: "\\s", with: "", options: .regularExpression)
    }
}
