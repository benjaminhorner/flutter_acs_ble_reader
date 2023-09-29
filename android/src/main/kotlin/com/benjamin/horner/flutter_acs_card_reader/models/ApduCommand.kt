package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.CardGen

data class ApduCommand (
    var command: String = "",
    var name: String = "",
    var debugName: String = "",
    var lengthMin: Int = 0,
    var lengthMax: Int = 0,
    var cardGen: CardGen = CardGen.GEN1
)