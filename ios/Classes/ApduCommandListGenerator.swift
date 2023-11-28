//
//  ApduCommandListGenerator.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 22/11/2023.
//

import Foundation

class ApduCommandListGenerator {
    private let TAG: String = "ApduCommandListGenerator"

    /* APDU Commands */
    private let APDU_SELECT_BY_MF_OR_EF: String = "00 A4 02 0C 02"
    private let APDU_SELECT_BY_DF: String = "00 A4 04 0C 06"

    private lazy var commonApduCommandList: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 3F 00",
            name: "MF",
            needsSignature: false,
            isEF: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 00 02",
            name: "EF_ICC",
            hexName: "00 02 00",
            hexNameGen2: "00 02 00",
            lengthMin: 25,
            lengthMax: 25,
            needsSignature: false,
            needsHash: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 00 05",
            name: "EF_IC",
            hexName: "00 05 00",
            hexNameGen2: "00 05 00",
            lengthMin: 8,
            lengthMax: 8,
            needsSignature: false,
            needsHash: false
        )
    ]

    private lazy var cardGen1List: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_DF) FF 54 41 43 48 4F",
            name: "DF_TACHOGRAPH",
            needsSignature: false,
            isEF: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 01",
            name: "EF_APP_IDENTIFICATION",
            lengthMin: 10,
            lengthMax: 10
        ),
    ]

    private lazy var cardGen2List: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_DF) FF 53 4D 52 44 54",
            name: "DF_TACHOGRAPH_G2",
            cardGen: .GEN2,
            needsSignature: false,
            isEF: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 01",
            name: "EF_APP_IDENTIFICATION",
            lengthMin: 17,
            lengthMax: 17,
            cardGen: .GEN2
        ),
    ]

    private lazy var apduList: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_DF) FF 54 41 43 48 4F",
            name: "DF_TACHOGRAPH",
            isEF: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 01",
            name: "EF_APP_IDENTIFICATION",
            hexName: "05 01 00",
            hexNameSigned: "05 01 01",
            lengthMin: 10,
            lengthMax: 10
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 20",
            name: "EF_IDENTIFICATION",
            hexName: "05 20 00",
            hexNameSigned: "05 20 01",
            lengthMin: 143,
            lengthMax: 143
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 0E",
            name: "EF_CARD_DOWNLOAD",
            hexName: "05 0E 00",
            hexNameSigned: "05 0E 01",
            lengthMin: 4,
            lengthMax: 4
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 21",
            name: "EF_DRIVING_LICENCE_INFO",
            hexName: "05 21 00",
            hexNameSigned: "05 21 01",
            lengthMin: 53,
            lengthMax: 53
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 07",
            name: "EF_CURRENT_USAGE",
            hexName: "05 07 00",
            hexNameSigned: "05 07 01",
            lengthMin: 19,
            lengthMax: 19
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 08",
            name: "EF_CONTROL_ACTIVITY_DATA",
            hexName: "05 08 00",
            hexNameSigned: "05 08 01",
            lengthMin: 46,
            lengthMax: 46
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 22",
            name: "EF_SPECIFIC_CONDITIONS",
            hexName: "05 22 00",
            hexNameSigned: "05 22 01",
            lengthMin: 280,
            lengthMax: 280,
            calculatedLength: 280,
            maxReadLoops: 1,
            remainingBytes: 25
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 00",
            name: "EF_CARD_CERTIFICATE",
            hexName: "C1 00 00",
            hexNameSigned: "C1 00 01",
            lengthMin: 194,
            lengthMax: 194,
            needsSignature: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 08",
            name: "EF_CA_CERTIFICATE",
            hexName: "C1 08 00",
            hexNameSigned: "C1 08 01",
            lengthMin: 194,
            lengthMax: 194,
            needsSignature: false
        )
    ]

    private lazy var apduTG2List: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_DF) FF 53 4D 52 44 54",
            name: "DF_TACHOGRAPH_G2",
            cardGen: .GEN2,
            isEF: false
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 01",
            name: "EF_APP_IDENTIFICATION",
            hexNameGen2: "05 01 02",
            hexNameSigned: "05 01 03",
            lengthMin: 17,
            lengthMax: 17,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 20",
            name: "EF_IDENTIFICATION",
            hexNameGen2: "05 20 02",
            hexNameSigned: "05 20 03",
            lengthMin: 143,
            lengthMax: 143,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 0E",
            name: "EF_CARD_DOWNLOAD",
            hexNameGen2: "05 0E 02",
            hexNameSigned: "05 0E 03",
            lengthMin: 4,
            lengthMax: 4,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 21",
            name: "EF_DRIVING_LICENCE_INFO",
            hexNameGen2: "05 21 02",
            hexNameSigned: "05 21 03",
            lengthMin: 53,
            lengthMax: 53,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 07",
            name: "EF_CURRENT_USAGE",
            hexNameGen2: "05 07 02",
            hexNameSigned: "05 07 03",
            lengthMin: 19,
            lengthMax: 19,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 08",
            name: "EF_CONTROL_ACTIVITY_DATA",
            hexNameGen2: "05 08 02",
            hexNameSigned: "05 08 03",
            lengthMin: 46,
            lengthMax: 46,
            cardGen: .GEN2
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 01",
            name: "EF_CARDSIGNCERTIFICATE",
            hexNameGen2: "C1 01 02",
            lengthMin: 204,
            lengthMax: 341,
            cardGen: .GEN2,
            needsSignature: false,
            isCertificat: true
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 00",
            name: "EF_CARD_CERTIFICATE",
            hexNameGen2: "C1 00 02",
            lengthMin: 204,
            lengthMax: 341,
            cardGen: .GEN2,
            needsSignature: false,
            isCertificat: true
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 08",
            name: "EF_CA_CERTIFICATE",
            hexNameGen2: "C1 08 02",
            lengthMin: 204,
            lengthMax: 341,
            cardGen: .GEN2,
            needsSignature: false,
            isCertificat: true
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) C1 09",
            name: "EF_LINK_CERTIFICATE",
            hexNameGen2: "C1 09 02",
            lengthMin: 204,
            lengthMax: 341,
            cardGen: .GEN2,
            needsSignature: false,
            isCertificat: true
        )
    ]
    
    private lazy var gen1VariableApduCommandsList: [ApduCommand] = [
       ApduCommand(
        selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 02",
        name: "EF_EVENTS_DATA",
        hexName: "05 02 00",
        hexNameSigned: "05 02 01",
        lengthMin: 864,
        lengthMax: 1728,
        remainingBytesMultiplier: 144,
        noOfVarType: NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE
       ),
       ApduCommand(
        selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 03",
        name: "EF_FAULTS_DATA",
        hexName: "05 03 00",
        hexNameSigned: "05 03 01",
        lengthMin: 576,
        lengthMax: 1152,
        remainingBytesMultiplier: 48,
        noOfVarType: NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE
       ),
       ApduCommand(
        selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 04",
        name: "EF_DRIVER_ACTIVITY_DATA",
        hexName: "05 04 00",
        hexNameSigned: "05 04 01",
        lengthMin: 5548,
        lengthMax: 13780,
        remainingBytesMultiplier: 1,
        remainingExtraBytes: 4,
        noOfVarType: NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE
       ),
       ApduCommand(
        selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 05",
        name: "EF_VEHICLES_USED",
        hexName: "05 05 00",
        hexNameSigned: "05 05 01",
        lengthMin: 2606,
        lengthMax: 6202,
        remainingBytesMultiplier: 31,
        remainingExtraBytes: 2,
        noOfVarType: NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS
       ),
       ApduCommand(
        selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 06",
        name: "EF_PLACES",
        hexName: "05 06 00",
        hexNameSigned: "05 06 01",
        lengthMin: 841,
        lengthMax: 1121,
        remainingBytesMultiplier: 10,
        remainingExtraBytes: 1,
        noOfVarType: NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS
       )
   ]
    
    private lazy var gen2VariableApduCommandsList: [ApduCommand] = [
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 02",
            name: "EF_EVENTS_DATA",
            hexNameGen2: "05 02 02",
            hexNameSigned: "05 02 03",
            lengthMin: 1584,
            lengthMax: 3168,
            remainingBytesMultiplier: 264,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE

        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 03",
            name: "EF_FAULTS_DATA",
            hexNameGen2: "05 03 02",
            hexNameSigned: "05 03 03",
            lengthMin: 576,
            lengthMax: 1152,
            remainingBytesMultiplier: 48,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 04",
            name: "EF_DRIVER_ACTIVITY_DATA",
            hexNameGen2: "05 04 02",
            hexNameSigned: "05 04 03",
            lengthMin: 5548,
            lengthMax: 13780,
            remainingBytesMultiplier: 1,
            remainingExtraBytes: 4,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 05",
            name: "EF_VEHICLES_USED",
            hexNameGen2: "05 05 02",
            hexNameSigned: "05 05 03",
            lengthMin: 4024,
            lengthMax: 9602,
            remainingBytesMultiplier: 48,
            remainingExtraBytes: 2,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 06",
            name: "EF_PLACES",
            hexNameGen2: "05 06 02",
            hexNameSigned: "05 06 03",
            lengthMin: 1766,
            lengthMax: 2354,
            remainingBytesMultiplier: 21,
            remainingExtraBytes: 2,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 22",
            name: "EF_SPECIFIC_CONDITIONS",
            hexNameGen2: "05 22 02",
            hexNameSigned: "05 22 03",
            lengthMin: 282,
            lengthMax: 562,
            remainingBytesMultiplier: 5,
            remainingExtraBytes: 2,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_SPECIFIC_CONDITIONS_RECORDS
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 23",
            name: "EF_VEHICLEUNITS_USED",
            hexNameGen2: "05 23 02",
            hexNameSigned: "05 23 03",
            lengthMin: 842,
            lengthMax: 2002,
            remainingBytesMultiplier: 10,
            remainingExtraBytes: 2,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_CARD_VEHICLE_UNIT_RECORDS
        ),
        ApduCommand(
            selectCommand: "\(APDU_SELECT_BY_MF_OR_EF) 05 24",
            name: "EF_GNSS_PLACES",
            hexNameGen2: "05 24 02",
            hexNameSigned: "05 24 03",
            lengthMin: 4538,
            lengthMax: 6050,
            remainingBytesMultiplier: 18,
            remainingExtraBytes: 2,
            cardGen: CardGen.GEN2,
            noOfVarType: NoOfVariablesEnum.NO_OF_GNSS_RECORDS
        )
    ]

    private func calculateRemainingBytes(apdu: ApduCommand, noOfVar: Int, totalBytes: Int) -> Int {
        let maxReadLoops: Int = totalBytes / 255
        let remainIngBytes: Int = totalBytes - (maxReadLoops * 255)
        print("\(apdu.name) totalBytes \(totalBytes) // maxReadLoops \(maxReadLoops) // remainIngBytes \(remainIngBytes)")
        return remainIngBytes
    }

    private func updateApdu(noOfVar: Int, apdu: ApduCommand) -> ApduCommand {
        var apduCommand: ApduCommand = apdu
        let totalBytes: Int = (noOfVar * apdu.remainingBytesMultiplier) + apdu.remainingExtraBytes
        apduCommand.remainingBytes = calculateRemainingBytes(apdu: apdu, noOfVar: noOfVar, totalBytes: totalBytes)
        apduCommand.calculatedLength = totalBytes
        apduCommand.maxReadLoops = totalBytes / 255
        return apduCommand
    }

    private func makeGen1List(noOfVarModel: NoOfVarModel) -> [ApduCommand] {
        let initialList: [ApduCommand] = commonApduCommandList + apduList
        var updatedVariableApduCommandsList: [ApduCommand] = []

        for apdu in gen1VariableApduCommandsList {
            switch apdu.noOfVarType {
            case .NO_OF_EVENTS_PER_TYPE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfEventsPerType, apdu: apdu))
            case .NO_OF_FAULTS_PER_TYPE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfFaultsPerType, apdu: apdu))
            case .NO_OF_CARD_VEHICLE_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfCardVehicleRecords, apdu: apdu))
            case .NO_OF_CARD_PLACE_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfCardPlaceRecords, apdu: apdu))
            case .CARD_ACTIVITY_LENGTH_RANGE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.cardActivityLengthRange, apdu: apdu))
            default:
                break
            }
        }

        let commandList: [ApduCommand] = initialList + updatedVariableApduCommandsList
        return commandList
    }

    private func makeGen2List(noOfVarModel: NoOfVarModel) -> [ApduCommand] {
        let initialList: [ApduCommand] = apduTG2List
        var updatedVariableApduCommandsList: [ApduCommand] = []

        NSLog("\(TAG) makeGen2List noOfVarModel // \(noOfVarModel)")

        for apdu in gen2VariableApduCommandsList {
            switch apdu.noOfVarType {
            case .NO_OF_EVENTS_PER_TYPE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfEventsPerType, apdu: apdu))
            case .NO_OF_FAULTS_PER_TYPE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfFaultsPerType, apdu: apdu))
            case .NO_OF_CARD_VEHICLE_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfCardVehicleRecords, apdu: apdu))
            case .NO_OF_CARD_PLACE_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfCardPlaceRecords, apdu: apdu))
            case .CARD_ACTIVITY_LENGTH_RANGE:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.cardActivityLengthRange, apdu: apdu))
            case .NO_OF_GNSS_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfGNSSRecords, apdu: apdu))
            case .NO_OF_CARD_VEHICLE_UNIT_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfCardVehicleUnitRecords, apdu: apdu))
            case .NO_OF_SPECIFIC_CONDITIONS_RECORDS:
                updatedVariableApduCommandsList.append(updateApdu(noOfVar: noOfVarModel.noOfSpecificConditionsRecords, apdu: apdu))
            default:
                break
            }
        }

        let commandList: [ApduCommand] = initialList + updatedVariableApduCommandsList
        return commandList
    }

    let apduTG2V2List: [ApduCommand] = []

    func cardVersionCommandList() -> [ApduCommand] {
        return commonApduCommandList + cardGen1List
    }

    func cardVersionGen2CommandList() -> [ApduCommand] {
        return commonApduCommandList + cardGen2List
    }

    func makeList(cardGen: CardGen, noOfVarModel: NoOfVarModel) -> [ApduCommand] {
        switch cardGen {
        case .GEN1:
            return makeGen1List(noOfVarModel: noOfVarModel)
        case .GEN2:
            return makeGen1List(noOfVarModel: noOfVarModel) + makeGen2List(noOfVarModel: noOfVarModel)
        case .GEN2V2:
            return commonApduCommandList + apduTG2V2List
        }
    }

    func calculateTotalUploadSteps(apduList: [ApduCommand]) -> Int {
        let cleanList: [ApduCommand] = apduList.filter { $0.isEF }
        for apdu in cleanList {
            NSLog("\(TAG) calculateTotalUploadSteps \(apdu.name)")
        }
        return cleanList.count
    }
}
