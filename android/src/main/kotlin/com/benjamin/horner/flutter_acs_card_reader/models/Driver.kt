package com.benjamin.horner.flutter_acs_card_reader

data class Driver (
    var card: String,
    var name: String = "",
    var firstName: String = "",
    var email: String = "",
    var phone: String = "",
    val agencyID: String = ""
)