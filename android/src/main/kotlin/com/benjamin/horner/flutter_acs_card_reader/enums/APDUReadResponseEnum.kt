package com.benjamin.horner.flutter_acs_card_reader

enum class APDUReadResponseEnum {
    SUCCESS,
    NO_EF_SELECTED,
    P1_LENGTH_GREATER_THAN_EF,
    P1_PLUS_LENGTH_GREATER_THAN_EF,
    FILE_ATTRIBUT_INTEGRITY_ERROR,
    STORED_DATE_INTEGRITY_ERROR
}