//
//  ACSError.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

enum ACSError: Error {
    case unableToTransmitApduException(description: String)
    case unableToCreateHashException(description: String)
    case unableToPerformSelection(description: String)
    case unableToConnectToCard(description: String)
    case hexDoesNotContainEnoughBytes(description: String)
    case unableToSignApdu(description: String)
    case unableToEncryptData(description: String)
}
