//
//  ApduCommand.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 22/11/2023.
//

import Foundation

struct ApduCommand {
    var selectCommand: String
    var readCommand: String
    var name: String
    var hexName: String
    var hexNameGen2: String
    var hexNameSigned: String
    var lengthMin: Int
    var lengthMax: Int
    var calculatedLength: Int
    var maxReadLoops: Int
    var remainingBytes: Int
    var remainingBytesMultiplier: Int
    var remainingExtraBytes: Int
    var cardGen: CardGen
    var needsSignature: Bool
    var isEF: Bool
    var needsHash: Bool
    var isCertificat: Bool
    var noOfVarType: NoOfVariablesEnum
    
    init(selectCommand: String = "", readCommand: String = "", name: String = "", hexName: String = "", hexNameGen2: String = "", hexNameSigned: String = "", lengthMin: Int = 0, lengthMax: Int = 0, calculatedLength: Int = 0, maxReadLoops: Int = 0, remainingBytes: Int = 0, remainingBytesMultiplier: Int = 0, remainingExtraBytes: Int = 0, cardGen: CardGen = CardGen.GEN1, needsSignature: Bool = true, isEF: Bool = true, needsHash: Bool = true, isCertificat: Bool = false, noOfVarType: NoOfVariablesEnum = NoOfVariablesEnum.NOT_A_VARIABLE) {
        self.selectCommand = selectCommand
        self.readCommand = readCommand
        self.name = name
        self.hexName = hexName
        self.hexNameGen2 = hexNameGen2
        self.hexNameSigned = hexNameSigned
        self.lengthMin = lengthMin
        self.lengthMax = lengthMax
        self.calculatedLength = calculatedLength
        self.maxReadLoops = maxReadLoops
        self.remainingBytes = remainingBytes
        self.remainingBytesMultiplier = remainingBytesMultiplier
        self.remainingExtraBytes = remainingExtraBytes
        self.cardGen = cardGen
        self.needsSignature = needsSignature
        self.isEF = isEF
        self.needsHash = needsHash
        self.isCertificat = isCertificat
        self.noOfVarType = noOfVarType
        
    }
}
