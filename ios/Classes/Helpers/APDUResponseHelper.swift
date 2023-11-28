//
//  APDUResponseHelper.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

class APDUResponseHelper {
    func selectResponseIntToAPDUReadResponse(response: Int?) -> APDUSelectResponseEnum {
        guard let response = response else {
            return .unknownError
        }
        switch response {
        case 0x90:
            return .success
        case 0x6A:
            return .notFound
        case 0x64, 0x65:
            return .fileAttributeIntegrityError
        default:
            return .unknownError
        }
    }

    func readResponseIntToAPDUReadResponse(response: Int?) -> APDUReadResponseEnum {
        guard let response = response else {
            return .storedDataIntegrityError
        }
        switch response {
        case 0x90:
            return .success
        case 0x67, 0x6C:
            return .offsetPlusLengthGreaterThanEF
        case 0x6B:
            return .offsetGreaterThanEF
        case 0x65:
            return .fileAttributeIntegrityError
        default:
            return .storedDataIntegrityError
        }
    }

    func hashResponseIntToAPDUReadResponse(response: Int?) -> APDUHashResponseEnum {
        guard let response = response else {
            return .fileAttributeIntegrityError
        }
        switch response {
        case 0x90:
            return .success
        case 0x69:
            return .notAllowed
        default:
            return .fileAttributeIntegrityError
        }
    }

    func signResponseIntToAPDUReadResponse(response: Int?) -> APDUSignResponseEnum {
        guard let response = response else {
            return .alteredImplicitSelectedKey
        }
        switch response {
        case 0x90:
            return .success
        case 0x69:
            return .missingHashOfFile
        default:
            return .alteredImplicitSelectedKey
        }
    }
}
