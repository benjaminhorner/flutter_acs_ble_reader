package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.CardGen

data class ApduCommand (
    var selectCommand: String = "",
    var readCommand: String = "",
    var name: String = "",
    var lengthMin: Int = 0,
    var lengthMax: Int = 0,
    var cardGen: CardGen = CardGen.GEN1,
    var needsSignature: Boolean = true,
    var isEF: Boolean = true
)