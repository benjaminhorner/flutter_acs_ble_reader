//
//  APDUSignResponseEnum.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

enum APDUSignResponseEnum {
    case success
    case missingHashOfFile
    case alteredImplicitSelectedKey
}
