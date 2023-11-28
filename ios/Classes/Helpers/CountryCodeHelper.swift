//
//  CountryCodeHelper.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

class CountryCodeHelper {
    func handleCountryCode(hex: String) -> String {
        let ccHex = String(hex.prefix(2))
        print("handleCountryCode \(ccHex)")
        return hexToCountryCode(ccHex)
    }

    private func hexToCountryCode(_ hex: String) -> String {
        guard let intValue = Int(hex, radix: 16) else {
            return "UNK"
        }

        switch intValue {
        case 0: return "___"
        case 1: return "A__"
        case 2: return "AL_"
        case 3: return "AND"
        case 4: return "ARM"
        case 5: return "AZ_"
        case 6: return "B__"
        case 7: return "BG_"
        case 8: return "BIH"
        case 9: return "BY_"
        case 10: return "CH_"
        case 11: return "CY_"
        case 12: return "CZ_"
        case 13: return "D__"
        case 14: return "DK_"
        case 15: return "E__"
        case 16: return "EST"
        case 17: return "F__"
        case 18: return "FIN"
        case 19: return "FL_"
        case 20: return "FR_"
        case 21: return "UK_"
        case 22: return "GE_"
        case 23: return "GR_"
        case 24: return "H__"
        case 25: return "HR_"
        case 26: return "I__"
        case 27: return "IRL"
        case 28: return "IS_"
        case 29: return "KZ_"
        case 30: return "L__"
        case 31: return "LT_"
        case 32: return "LV_"
        case 33: return "M__"
        case 34: return "MC_"
        case 35: return "MD_"
        case 36: return "MK_"
        case 37: return "N__"
        case 38: return "NL_"
        case 39: return "P__"
        case 40: return "PL_"
        case 41: return "RO_"
        case 42: return "RSM"
        case 43: return "RUS"
        case 44: return "S__"
        case 45: return "SK_"
        case 46: return "SLO"
        case 47: return "TM_"
        case 48: return "TR_"
        case 49: return "UA_"
        case 50: return "V__"
        case 51: return "YU_"
        case 253: return "EC_"
        case 254: return "EUR"
        case 255: return "WLD"
        default: return "UNK"
        }
    }
}

