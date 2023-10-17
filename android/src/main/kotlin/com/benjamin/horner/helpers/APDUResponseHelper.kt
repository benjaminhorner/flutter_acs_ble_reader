package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.APDUSelectResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUReadResponseEnum

class APDUResponseHelper {
    fun selectResponseIntToAPDUReadResponse(response: Int?) : APDUSelectResponseEnum {
        when(response) {
            0x90 -> {
                return APDUSelectResponseEnum.SUCCESS
            }
            0x6A -> {
                return APDUSelectResponseEnum.NOT_FOUND
            }
            0x64 -> {
                return APDUSelectResponseEnum.FILE_ATTRIBUT_INTEGRITY_ERROR
            }
            0x65 -> {
                return APDUSelectResponseEnum.FILE_ATTRIBUT_INTEGRITY_ERROR
            }
            else -> return APDUSelectResponseEnum.UNKNOWN_ERROR
        }
    }

    fun readResponseIntToAPDUReadResponse(response: Int?) : APDUReadResponseEnum {
        when(response) {
            0x90 -> {
                return APDUReadResponseEnum.SUCCESS
            }
            0x67 -> {
                return APDUReadResponseEnum.P1_PLUS_LENGTH_GREATER_THAN_EF
            }
            0x6C -> {
                return APDUReadResponseEnum.P1_PLUS_LENGTH_GREATER_THAN_EF
            }
            0x6B -> {
                return APDUReadResponseEnum.P1_LENGTH_GREATER_THAN_EF
            }
            0x65 -> {
                return APDUReadResponseEnum.FILE_ATTRIBUT_INTEGRITY_ERROR
            }
            else -> {
                return APDUReadResponseEnum.STORED_DATE_INTEGRITY_ERROR
            }
        }
    }
}