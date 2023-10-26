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
            needsHash = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 05",
            name = "EF_IC",
            lengthMin = 8,
            lengthMax = 8,
            needsSignature = false,
            needsHash = false
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
            lengthMax = 10
            hexName = "050100",
            hexNameSigned = "050101"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
            name = "EF_IDENTIFICATION",
            lengthMin = 143,
            lengthMax = 143,
            hexName = "052000",
            hexNameSigned = "052001"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
            name = "EF_CARD_DOWNLOAD",
            lengthMin = 4,
            lengthMax = 4,
            hexName = "050E00",
            hexNameSigned = "050E01"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
            name = "EF_DRIVING_LICENCE_INFO",
            lengthMin = 53,
            lengthMax = 53,
            hexName = "052100",
            hexNameSigned = "052101"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
            name = "EF_CURRENT_USAGE",
            lengthMin = 19,
            lengthMax = 19,
            hexName = "050700",
            hexNameSigned = "050701"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
            name = "EF_CONTROL_ACTIVITY_DATA",
            lengthMin = 46,
            lengthMax = 46,
            hexName = "050800",
            hexNameSigned = "050801"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
            name = "EF_SPECIFIC_CONDITIONS",
            lengthMin = 280,
            lengthMax = 280,
            hexName = "052200",
            hexNameSigned = "052201"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
            name = "EF_CARD_CERTIFICATE",
            lengthMin = 194,
            lengthMax = 194,
            needsSignature = false,
            hexName = "C10000",
            hexNameSigned = "C10001"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
            name = "EF_CA_CERTIFICATE",
            lengthMin = 194,
            lengthMax = 194,
            needsSignature = false,
            hexName = "C10800",
            hexNameSigned = "C10801"
        )
    )

    private val apduTG2List: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_DF} FF 53 4D 52 44 54",
            name = "DF_TACHOGRAPH_G2",
            isEF = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
            name = "EF_APP_IDENTIFICATION",
            lengthMin = 17,
            lengthMax = 17,
            hexName = "050102",
            hexNameSigned = "050103"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
            name = "EF_IDENTIFICATION",
            lengthMin = 143,
            lengthMax = 143,
            hexName = "052002",
            hexNameSigned = "052003"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
            name = "EF_CARD_DOWNLOAD",
            lengthMin = 4,
            lengthMax = 4,
            hexName = "050E02",
            hexNameSigned = "050E03"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
            name = "EF_DRIVING_LICENCE_INFO",
            lengthMin = 53,
            lengthMax = 53,
            hexName = "052102",
            hexNameSigned = "052103"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
            name = "EF_CURRENT_USAGE",
            lengthMin = 19,
            lengthMax = 19,
            hexName = "050702",
            hexNameSigned = "050703"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
            name = "EF_CONTROL_ACTIVITY_DATA",
            lengthMin = 46,
            lengthMax = 46,
            hexName = "050802",
            hexNameSigned = "050803"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 01",
            name = "EF_CARDSIGNCERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false,
            isCertificat = true,
            hexName = "C10102"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
            name = "EF_CARD_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false,
            isCertificat = true,
            hexName = "C10002"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
            name = "EF_CA_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false,
            isCertificat = true,
            hexName = "C10802"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 09",
            name = "EF_LINK_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false,
            isCertificat = true,
            hexName = "C10902"
        ),
    )

    private val gen1VariableApduCommandsList: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
            name = "EF_EVENTS_DATA",
            lengthMin = 864,
            lengthMax = 1728,
            noOfVarType = NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE,
            remainingBytesMultiplier = 144,
            hexName = "050200",
            hexNameSigned = "050201"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
            name = "EF_FAULTS_DATA",
            lengthMin = 576,
            lengthMax = 1152,
            noOfVarType = NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE,
            remainingBytesMultiplier = 48,
            hexName = "050300",
            hexNameSigned = "050301"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
            name = "EF_DRIVER_ACTIVITY_DATA",
            lengthMin = 5548,
            lengthMax = 13780,
            noOfVarType = NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE,
            remainingBytesMultiplier = 1,
            remainingExtraBytes = 4,
            hexName = "050400",
            hexNameSigned = "050401"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
            name = "EF_VEHICULES_USED",
            lengthMin = 2606,
            lengthMax = 6202,
            noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS,
            remainingBytesMultiplier = 31,
            remainingExtraBytes = 2,
            hexName = "050500",
            hexNameSigned = "050501"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
            name = "EF_PLACES",
            lengthMin = 841,
            lengthMax = 1121,
            noOfVarType = NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS,
            remainingBytesMultiplier = 10,
            remainingExtraBytes = 1,
            hexName = "050600",
            hexNameSigned = "050601"
        ),
    )
    
    private val gen2VariableApduCommandsList: List<ApduCommand> = listOf(
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
            name = "EF_EVENTS_DATA",
            lengthMin = 1584,
            lengthMax = 3168,
            noOfVarType = NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE,
            remainingBytesMultiplier = 264,
            hexName = "050202",
            hexNameSigned = "050203"

        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
            name = "EF_FAULTS_DATA",
            lengthMin = 576,
            lengthMax = 1152,
            noOfVarType = NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE,
            remainingBytesMultiplier = 48,
            hexName = "050302",
            hexNameSigned = "050303"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
            name = "EF_DRIVER_ACTIVITY_DATA",
            lengthMin = 5548,
            lengthMax = 13780,
            noOfVarType = NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE,
            remainingBytesMultiplier = 1,
            remainingExtraBytes = 4,
            hexName = "050402",
            hexNameSigned = "050403"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
            name = "EF_VEHICULES_USED",
            lengthMin = 4024,
            lengthMax = 5602,
            noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS,
            remainingBytesMultiplier = 48,
            remainingExtraBytes = 2,
            hexName = "050502",
            hexNameSigned = "050503"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
            name = "EF_PLACES",
            lengthMin = 1766,
            lengthMax = 2354,
            noOfVarType = NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS,
            remainingBytesMultiplier = 21,
            remainingExtraBytes = 2,
            hexName = "050602",
            hexNameSigned = "050603"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
            name = "EF_SPECIFIC_CONDITIONS",
            lengthMin = 282,
            lengthMax = 562,
            noOfVarType = NoOfVariablesEnum.NO_OF_SPECIFIC_CONDITIONS_RECORDS,
            remainingBytesMultiplier = 10,
            remainingExtraBytes = 2,
            hexName = "052202",
            hexNameSigned = "052203"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 23",
            name = "EF_VEHICULEUNITS_USED",
            lengthMin = 842,
            lengthMax = 2002,
            noOfVarType = NoOfVariablesEnum.NO_OF_CARD_VEHICLE_UNIT_RECORDS,
            remainingBytesMultiplier = 15,
            remainingExtraBytes = 2,
            hexName = "052302",
            hexNameSigned = "052303"
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 24",
            name = "EF_GNSS_PLACES",
            lengthMin = 3782,
            lengthMax = 5042,
            noOfVarType = NoOfVariablesEnum.NO_OF_GNSS_RECORDS,
            remainingBytesMultiplier = 15,
            remainingExtraBytes = 2,
            hexName = "052402",
            hexNameSigned = "052403"
        ),
    )

    private fun calculateRemainingBytes(
        apdu: ApduCommand,
        noOfVar: Int
    ): Int {
        val totalBytes: Int = (noOfVar * apdu.remainingBytesMultiplier) + apdu.remainingExtraBytes
        val offsetBytes: Int = totalBytes / 255
        val maxReadLoops: Int = offsetBytes.toInt()
        val remainIngBytes: Int = totalBytes - (maxReadLoops * 255)
        Log.e(TAG, "totalBytes $totalBytes // offsetBytes $offsetBytes // maxReadLoops $maxReadLoops // remainIngBytes $remainIngBytes")
        return remainIngBytes
    }


    private fun makeGen1List(
        noOfVarModel: NoOfVarModel,
    ): List<ApduCommand> {
        val initialList: List<ApduCommand> = commonApduCommandList.plus(apduList)
        val updatedVariableApduCommandsList: MutableList<ApduCommand> = mutableListOf()

        for (apdu in gen1VariableApduCommandsList) {
            if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_EVENTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfEventsPerType)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfFaultsPerType)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleRecords)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardPlaceRecords)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.cardActivityLengthRange)
                updatedVariableApduCommandsList.add(apdu)
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
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_FAULTS_PER_TYPE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfFaultsPerType)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleRecords)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_PLACE_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardPlaceRecords)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.CARD_ACTIVITY_LENGTH_RANGE) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.cardActivityLengthRange)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_GNSS_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfGNSSRecords)
                updatedVariableApduCommandsList.add(apdu)
            } else if (apdu.noOfVarType == NoOfVariablesEnum.NO_OF_CARD_VEHICLE_UNIT_RECORDS) {
                apdu.remainingBytes = calculateRemainingBytes(apdu, noOfVarModel.noOfCardVehicleUnitRecords)
                updatedVariableApduCommandsList.add(apdu)
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
            CardGen.GEN2 -> return makeGen2List(noOfVarModel)
            CardGen.GEN2V2 -> return commonApduCommandList.plus(apduTG2V2List)
            else -> return commonApduCommandList.plus(apduList)
        }
    }
}