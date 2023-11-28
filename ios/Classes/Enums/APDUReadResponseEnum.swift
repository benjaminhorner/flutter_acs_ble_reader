//
//  APDUReadResponseEnum.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

enum APDUReadResponseEnum {
    case success
    case offsetPlusLengthGreaterThanEF
    case offsetGreaterThanEF
    case fileAttributeIntegrityError
    case storedDataIntegrityError
}
