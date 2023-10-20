package com.benjamin.horner.flutter_acs_card_reader

data class ApduData (
    var name: String = "",
    var data: String = "",
    var offset: Int = 0,
    var length: Int = 255
)