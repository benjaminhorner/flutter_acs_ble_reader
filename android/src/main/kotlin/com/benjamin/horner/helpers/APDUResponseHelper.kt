package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.APDUSelectResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUReadResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUHashResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUSignResponseEnum

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
                return APDUReadResponseEnum.OFFSET_PLUS_LENGTH_GREATER_THAN_EF
            }
            0x6C -> {
                return APDUReadResponseEnum.OFFSET_PLUS_LENGTH_GREATER_THAN_EF
            }
            0x6B -> {
                return APDUReadResponseEnum.OFFSET_LENGTH_GREATER_THAN_EF
            }
            0x65 -> {
                return APDUReadResponseEnum.FILE_ATTRIBUT_INTEGRITY_ERROR
            }
            else -> {
                return APDUReadResponseEnum.STORED_DATE_INTEGRITY_ERROR
            }
        }
    }

    fun hashResponseIntToAPDUReadResponse(response: Int?) : APDUHashResponseEnum {
        when(response) {
            0x90 -> {
                return APDUHashResponseEnum.SUCCESS
            }
            0x69 -> {
                return APDUHashResponseEnum.NOT_ALLOWED
            }
            else -> {
                return APDUHashResponseEnum.FILE_ATTRIBUT_INTEGRITY_ERROR
            }
        }
    }

    fun signResponseIntToAPDUReadResponse(response: Int?) : APDUSignResponseEnum {
        when(response) {
            0x90 -> {
                return APDUSignResponseEnum.SUCCESS
            }
            0x69 -> {
                return APDUSignResponseEnum.MISSING_HASH_OF_FILE
            }
            else -> {
                return APDUSignResponseEnum.ALTERED_IMPLICIT_SELECTED_KEY
            }
        }
    }
}