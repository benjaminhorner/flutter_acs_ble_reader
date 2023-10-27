package com.benjamin.horner.flutter_acs_card_reader

class StringHelper {
    fun removeWhitespaces(input: String): String {
        return input.replace("\\s".toRegex(), "")
    }
}