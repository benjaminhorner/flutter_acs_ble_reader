package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.CardGen
import com.benjamin.horner.flutter_acs_card_reader.NoOfVariablesEnum

data class ApduCommand (
    var selectCommand: String = "",
    var readCommand: String = "",
    var name: String = "",
    var hexName: String = "",
    var hexNameGen2: String = "",
    var hexNameSigned: String = "",
    var lengthMin: Int = 0,
    var lengthMax: Int = 0,
    var remainingBytes: Int = 0,
    var remainingBytesMultiplier: Int = 0,
    var remainingExtraBytes: Int = 0,
    var cardGen: CardGen = CardGen.GEN1,
    var needsSignature: Boolean = true,
    var isEF: Boolean = true,
    var needsHash: Boolean = true,
    var isCertificat: Boolean = false,
    var noOfVarType: NoOfVariablesEnum = NoOfVariablesEnum.NOT_A_VARIABLE
)