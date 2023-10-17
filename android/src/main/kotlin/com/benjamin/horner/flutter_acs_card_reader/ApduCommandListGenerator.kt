package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.ApduCommand
import com.benjamin.horner.flutter_acs_card_reader.CardGen
import com.benjamin.horner.flutter_acs_card_reader.NoOfVarModel

// JavaX
import javax.smartcardio.CardChannel

class ApduCommandListGenerator {
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
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 05",
            name = "EF_IC",
            lengthMin = 8,
            lengthMax = 8,
            needsSignature = false
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
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
            name = "EF_IDENTIFICATION",
            lengthMin = 143,
            lengthMax = 143
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
            name = "EF_CARD_DOWNLOAD",
            lengthMin = 4,
            lengthMax = 4
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
            name = "EF_DRIVING_LICENCE_INFO",
            lengthMin = 53,
            lengthMax = 53
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
            name = "EF_EVENTS_DATA",
            lengthMin = 864,
            lengthMax = 1728
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
            name = "EF_FAULTS_DATA",
            lengthMin = 576,
            lengthMax = 1152
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
            name = "EF_DRIVER_ACTIVITY_DATA",
            lengthMin = 5548,
            lengthMax = 13780
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
            name = "EF_VEHICULES_USED",
            lengthMin = 2606,
            lengthMax = 6202
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
            name = "EF_PLACES",
            lengthMin = 841,
            lengthMax = 1121
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
            name = "EF_CURRENT_USAGE",
            lengthMin = 19,
            lengthMax = 19
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
            name = "EF_CONTROL_ACTIVITY_DATA",
            lengthMin = 46,
            lengthMax = 46
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
            name = "EF_SPECIFIC_CONDITIONS",
            lengthMin = 280,
            lengthMax = 280
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
            name = "EF_CARD_CERTIFICATE",
            lengthMin = 194,
            lengthMax = 194,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
            name = "EF_CA_CERTIFICATE",
            lengthMin = 194,
            lengthMax = 194,
            needsSignature = false
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
            lengthMax = 17
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
            name = "EF_IDENTIFICATION",
            lengthMin = 143,
            lengthMax = 143
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 0E",
            name = "EF_CARD_DOWNLOAD",
            lengthMin = 4,
            lengthMax = 4
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 21",
            name = "EF_DRIVING_LICENCE_INFO",
            lengthMin = 53,
            lengthMax = 53
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 02",
            name = "EF_EVENTS_DATA",
            lengthMin = 1584,
            lengthMax = 3168
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 03",
            name = "EF_FAULTS_DATA",
            lengthMin = 576,
            lengthMax = 1152
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 04",
            name = "EF_DRIVER_ACTIVITY_DATA",
            lengthMin = 5548,
            lengthMax = 13780
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 05",
            name = "EF_VEHICULES_USED",
            lengthMin = 4024,
            lengthMax = 5602
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 06",
            name = "EF_PLACES",
            lengthMin = 1766,
            lengthMax = 2354
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 07",
            name = "EF_CURRENT_USAGE",
            lengthMin = 19,
            lengthMax = 19
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 08",
            name = "EF_CONTROL_ACTIVITY_DATA",
            lengthMin = 46,
            lengthMax = 46
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 22",
            name = "EF_SPECIFIC_CONDITIONS",
            lengthMin = 282,
            lengthMax = 562
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 00",
            name = "EF_CARD_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 08",
            name = "EF_CA_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 23",
            name = "EF_VEHICULEUNITS_USED",
            lengthMin = 842,
            lengthMax = 2002
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 24",
            name = "EF_GNSS_PLACES",
            lengthMin = 3782,
            lengthMax = 5042
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 01",
            name = "EF_CARDSIGNCERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false
        ),
        ApduCommand(
            selectCommand = "${APDU_SELECT_BY_MF_OR_EF} C1 09",
            name = "EF_LINK_CERTIFICATE",
            lengthMin = 204,
            lengthMax = 341,
            needsSignature = false
        ),
    )

    val apduTG2V2List: List<ApduCommand> = listOf()

    fun cardVersionCommandList(): List<ApduCommand> {
        return commonApduCommandList.plus(cardGen1List)
    }

    fun cardVersionGen2CommandList(): List<ApduCommand> {
        return commonApduCommandList.plus(cardGen2List)
    }

    fun makeList(cardGen: CardGen, noOfVarModel: NoOfVarModel): List<ApduCommand> {
        when(cardGen) {
            CardGen.GEN1 -> return commonApduCommandList.plus(apduList)
            CardGen.GEN2 -> return commonApduCommandList.plus(apduTG2List)
            CardGen.GEN2V2 -> return commonApduCommandList.plus(apduTG2V2List)
            else -> return commonApduCommandList.plus(apduList)
        }
    }
}