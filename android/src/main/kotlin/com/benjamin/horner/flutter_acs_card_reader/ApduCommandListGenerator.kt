package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.ApduCommand
import com.benjamin.horner.flutter_acs_card_reader.CardGen
import com.benjamin.horner.flutter_acs_card_reader.NoOfVarModel
import com.benjamin.horner.flutter_acs_card_reader.NoOfVariablesEnum
import kotlin.math.floor
import android.util.Log

// JavaX
import javax.smartcardio.CardChannel

class ApduCommandListGenerator {
    private val TAG: String = "ApduCommandListGenerator"

    /* APDU Commands */
    private val APDU_SELECT_BY_MF_OR_EF: String = "00 A4 02 0C 02"
    private val APDU_SELECT_BY_DF: String = "00 A4 04 0C 06"

    private val commonApduCommandList: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 3F 00",
            name = "MF",
            isEF = false,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 02",
            name = "EF_ICC",
            lengthMin = 25,
            lengthMax = 25,
            needsSignature = false,
            needsHash = false,
            hexName = "00 02 00",
            hexNameGen2 = "00 02 02"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 05",
            name = "EF_IC",
            lengthMin = 8,
            lengthMax = 8,
            needsSignature = false,
            needsHash = false,
            hexName = "00 05 00",
            hexNameGen2 = "00 05 02"
        )
    )
    
    private val cardGen1List: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_DF} FF 54 41 43 48 4F",
            name = "DF_TACHOGRAPH",
            isEF = false,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
            name = "EF_APP_IDENTIFICATION",
            lengthMin = 10,
            lengthMax = 10
        ),
    )

    private val cardGen2List: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_DF} FF 53 4D 52 44 54",
            name = "DF_TACHOGRAPH_G2",
            isEF = false,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
            name = "EF_APP_IDENTIFICATION",
            lengthMin = 17,
            lengthMax = 17
        ),
    )

    private val apduList: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_DF} FF 54 41 43 48 4F",
            name = "DF_TACHOGRAPH",
            isEF = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
            name = "EF_APP_IDENTIFICATION",
            lengthMin = 10,
            lengthMax = 10,
            hexName = "05 01 00",
            hexNameSigned = "05 01 01"
        ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
        //     name = "EF_IDENTIFICATION",
        //     lengthMin = 143,
        //     lengthMax = 143,
        //     hexName = "05 20 00",
        //     hexNameSigned = "05 20 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
        //     name = "EF_CARD_DOWNLOAD",
        //     lengthMin = 4,
        //     lengthMax = 4,
        //     hexName = "05 0E 00",
        //     hexNameSigned = "05 0E 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
        //     name = "EF_DRIVING_LICENCE_INFO",
        //     lengthMin = 53,
        //     lengthMax = 53,
        //     hexName = "05 21 00",
        //     hexNameSigned = "05 21 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
        //     name = "EF_CURRENT_USAGE",
        //     lengthMin = 19,
        //     lengthMax = 19,
        //     hexName = "05 07 00",
        //     hexNameSigned = "05 07 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
        //     name = "EF_CONTROL_ACTIVITY_DATA",
        //     lengthMin = 46,
        //     lengthMax = 46,
        //     hexName = "05 08 00",
        //     hexNameSigned = "05 08 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
        //     name = "EF_SPECIFIC_CONDITIONS",
        //     lengthMin = 280,
        //     lengthMax = 280,
        //     hexName = "05 22 00",
        //     hexNameSigned = "05 22 01",
        //     remainingBytes = 24
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
        //     name = "EF_CARD_CERTIFICATE",
        //     lengthMin = 194,
        //     lengthMax = 194,
        //     needsSignature = false,
        //     hexName = "C10000",
        //     hexNameSigned = "C1 00 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
        //     name = "EF_CA_CERTIFICATE",
        //     lengthMin = 194,
        //     lengthMax = 194,
        //     needsSignature = false,
        //     hexName = "C1 08 00",
        //     hexNameSigned = "C1 08 01"
        // )
    )

    private val apduTG2List: List<ApduCommand> = listOf(
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_DF} FF 53 4D 52 44 54",
        //     name = "DF_TACHOGRAPH_G2",
        //     isEF = false
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
        //     name = "EF_APP_IDENTIFICATION",
        //     lengthMin = 17,
        //     lengthMax = 17,
        //     hexNameGen2 = "05 01 02",
        //     hexNameSigned = "05 01 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
        //     name = "EF_IDENTIFICATION",
        //     lengthMin = 143,
        //     lengthMax = 143,
        //     hexNameGen2 = "05 20 02",
        //     hexNameSigned = "05 20 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
        //     name = "EF_CARD_DOWNLOAD",
        //     lengthMin = 4,
        //     lengthMax = 4,
        //     hexNameGen2 = "05 0E 02",
        //     hexNameSigned = "05 0E 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
        //     name = "EF_DRIVING_LICENCE_INFO",
        //     lengthMin = 53,
        //     lengthMax = 53,
        //     hexNameGen2 = "05 21 02",
        //     hexNameSigned = "05 21 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
        //     name = "EF_CURRENT_USAGE",
        //     lengthMin = 19,
        //     lengthMax = 19,
        //     hexNameGen2 = "05 07 02",
        //     hexNameSigned = "05 07 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
        //     name = "EF_CONTROL_ACTIVITY_DATA",
        //     lengthMin = 46,
        //     lengthMax = 46,
        //     hexNameGen2 = "05 08 02",
        //     hexNameSigned = "05 08 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 01",
        //     name = "EF_CARDSIGNCERTIFICATE",
        //     lengthMin = 204,
        //     lengthMax = 341,
        //     needsSignature = false,
        //     isCertificat = true,
        //     hexNameGen2 = "C1 01 02"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
        //     name = "EF_CARD_CERTIFICATE",
        //     lengthMin = 204,
        //     lengthMax = 341,
        //     needsSignature = false,
        //     isCertificat = true,
        //     hexNameGen2 = "C1 00 02"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
        //     name = "EF_CA_CERTIFICATE",
        //     lengthMin = 204,
        //     lengthMax = 341,
        //     needsSignature = false,
        //     isCertificat = true,
        //     hexNameGen2 = "C1 08 02"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 09",
        //     name = "EF_LINK_CERTIFICATE",
        //     lengthMin = 204,
        //     lengthMax = 341,
        //     needsSignature = false,
        //     isCertificat = true,
        //     hexNameGen2 = "C1 09 02"
        // ),
    )

    private val gen1VariableApduCommandsList: List<ApduCommand> = listOf(
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
        //     name = "EF_EVENTS_DATA",
        //     lengthMin = 864,
        //     lengthMax = 1728,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE,
        //     remainingBytesMultiplier = 144,
        //     hexName = "05 02 00",
        //     hexNameSigned = "05 02 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
        //     name = "EF_FAULTS_DATA",
        //     lengthMin = 576,
        //     lengthMax = 1152,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE,
        //     remainingBytesMultiplier = 48,
        //     hexName = "05 03 00",
        //     hexNameSigned = "05 03 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
        //     name = "EF_DRIVER_ACTIVITY_DATA",
        //     lengthMin = 5548,
        //     lengthMax = 13780,
        //     noOfVarType = NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE,
        //     remainingBytesMultiplier = 1,
        //     remainingExtraBytes = 4,
        //     hexName = "05 04 00",
        //     hexNameSigned = "05 04 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
        //     name = "EF_VEHICULES_USED",
        //     lengthMin = 2606,
        //     lengthMax = 6202,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS,
        //     remainingBytesMultiplier = 31,
        //     remainingExtraBytes = 2,
        //     hexName = "05 05 00",
        //     hexNameSigned = "05 05 01"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
        //     name = "EF_PLACES",
        //     lengthMin = 841,
        //     lengthMax = 1121,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS,
        //     remainingBytesMultiplier = 10,
        //     remainingExtraBytes = 1,
        //     hexName = "05 06 00",
        //     hexNameSigned = "05 06 01"
        // ),
    )
    
    private val gen2VariableApduCommandsList: List<ApduCommand> = listOf(
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
        //     name = "EF_EVENTS_DATA",
        //     lengthMin = 1584,
        //     lengthMax = 3168,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE,
        //     remainingBytesMultiplier = 264,
        //     hexNameGen2 = "05 02 02",
        //     hexNameSigned = "05 02 03"

        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
        //     name = "EF_FAULTS_DATA",
        //     lengthMin = 576,
        //     lengthMax = 1152,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE,
        //     remainingBytesMultiplier = 48,
        //     hexNameGen2 = "05 03 02",
        //     hexNameSigned = "05 03 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
        //     name = "EF_DRIVER_ACTIVITY_DATA",
        //     lengthMin = 5548,
        //     lengthMax = 13780,
        //     noOfVarType = NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE,
        //     remainingBytesMultiplier = 1,
        //     remainingExtraBytes = 4,
        //     hexNameGen2 = "05 04 02",
        //     hexNameSigned = "05 04 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
        //     name = "EF_VEHICULES_USED",
        //     lengthMin = 4024,
        //     lengthMax = 5602,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS,
        //     remainingBytesMultiplier = 48,
        //     remainingExtraBytes = 2,
        //     hexNameGen2 = "05 05 02",
        //     hexNameSigned = "05 05 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
        //     name = "EF_PLACES",
        //     lengthMin = 1766,
        //     lengthMax = 2354,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS,
        //     remainingBytesMultiplier = 21,
        //     remainingExtraBytes = 2,
        //     hexNameGen2 = "05 06 02",
        //     hexNameSigned = "05 06 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
        //     name = "EF_SPECIFIC_CONDITIONS",
        //     lengthMin = 282,
        //     lengthMax = 562,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_SPECIFIC_CONDITIONS_RECORDS,
        //     remainingBytesMultiplier = 10,
        //     remainingExtraBytes = 2,
        //     hexNameGen2 = "05 22 02",
        //     hexNameSigned = "05 22 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 23",
        //     name = "EF_VEHICULEUNITS_USED",
        //     lengthMin = 842,
        //     lengthMax = 2002,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_UNIT_RECORDS,
        //     remainingBytesMultiplier = 15,
        //     remainingExtraBytes = 2,
        //     hexNameGen2 = "05 23 02",
        //     hexNameSigned = "05 23 03"
        // ),
        // ApduCommand(
        //     selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 24",
        //     name = "EF_GNSS_PLACES",
        //     lengthMin = 4538,
        //     lengthMax = 6050,
        //     noOfVarType = NoOfVariablesEnum.NO_OF_GNSS_RECORDS,
        //     remainingBytesMultiplier = 18,
        //     remainingExtraBytes = 2,
        //     hexNameGen2 = "05 24 02",
        //     hexNameSigned = "05 24 03"
        // ),
    )

    private fun calculateRemainingBytes(
        apdu: ApduCommand,
        noOfVar: Int
    ): Int {
        val totalBytes: Int = (noOfVar * apdu.remainingBytesMultiplier) + apdu.remainingExtraBytes
        val maxReadLoops: Int = totalBytes / 256
        val remainIngBytes: Int = totalBytes - (maxReadLoops * 256)
        Log.e(TAG, "${apdu.name} totalBytes $totalBytes // maxReadLoops $maxReadLoops // remainIngBytes $remainIngBytes")
        return remainIngBytes
    }


    private fun makeGen1List(
        noOfVarModel: NoOfVarModel,
    ): List<ApduCommand> {
        val initialList: List<ApduCommand> = commonApduCommandList.plus(apduList)
        val updatedVariableApduCommandsList: MutableList<ApduCommand> = mutableListOf()

        Log.e("$TAG makeGen1List", "$noOfVarModel")

        for (apdu in gen1VariableApduCommandsList) {
            if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfEventsPerType)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen1List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfFaultsPerType)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen1List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen1List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardPlaceRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen1List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.cardActivityLengthRange)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen1List", "${apdu.name} // ${apdu.remainingBytes}")
            }
        }

        val commandList: List<ApduCommand> = initialList.plus(updatedVariableApduCommandsList)
        return commandList
    }

    private fun makeGen2List(
        noOfVarModel: NoOfVarModel,
    ): List<ApduCommand> {
        val initialList: List<ApduCommand> = commonApduCommandList.plus(apduTG2List)
        val updatedVariableApduCommandsList: MutableList<ApduCommand> = mutableListOf()

        for (apdu in gen2VariableApduCommandsList) {
            if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfEventsPerType)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfFaultsPerType)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardPlaceRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.cardActivityLengthRange)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_GNSS_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfGNSSRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_UNIT_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleUnitRecords)
                updatedVariableApduCommandsList.add(apdu)
                Log.e("$TAG makeGen2List", "${apdu.name} // ${apdu.remainingBytes}")
            }
        }

        val commandList: List<ApduCommand> = initialList.plus(updatedVariableApduCommandsList)
        return commandList
    }

    val apduTG2V2List: List<ApduCommand> = listOf()

    fun cardVersionCommandList(): List<ApduCommand> {
        return commonApduCommandList.plus(cardGen1List)
    }

    fun cardVersionGen2CommandList(): List<ApduCommand> {
        return commonApduCommandList.plus(cardGen2List)
    }

    fun makeList(cardGen: CardGen,
        noOfVarModel: NoOfVarModel,
    ): List<ApduCommand> {
        when(cardGen) {
            CardGen.GEN1 -> return makeGen1List(noOfVarModel)
            CardGen.GEN2 -> return makeGen1List(noOfVarModel).plus(makeGen2List(noOfVarModel))
            CardGen.GEN2V2 -> return commonApduCommandList.plus(apduTG2V2List)
            else -> return commonApduCommandList.plus(apduList)
        }
    }

    fun calculateTotalUploadSteps(apduList: List<ApduCommand>): Int {
        val cleanList: MutableList<ApduCommand> = mutableListOf()
        for (apdu in apduList) {
            if (apdu.isEF) {
               cleanList.add(apdu) 
            }
        }
        return cleanList.size
    }
}