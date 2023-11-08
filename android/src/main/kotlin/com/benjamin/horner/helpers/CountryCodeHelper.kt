package com.benjamin.horner.flutter_acs_card_reader

import android.util.Log

class CountryCodeHelper {
    fun handleCountryCode(hex: String): String {
        val ccHex: String = hex.substring(0, 2)
        Log.e("handleCountryCode", "$ccHex")
        return hexToCountryCode(ccHex)
    }

    private fun hexToCountryCode(hex: String): String {
        return when (Integer.parseInt(hex, 16)) {
            0 -> "___"
            1 -> "A__"
            2 -> "AL_"
            3 -> "AND"
            4 -> "ARM"
            5 -> "AZ_"
            6 -> "B__"
            7 -> "BG_"
            8 -> "BIH"
            9 -> "BY_"
            10 -> "CH_"
            11 -> "CY_"
            12 -> "CZ_"
            13 -> "D__"
            14 -> "DK_"
            15 -> "E__"
            16 -> "EST"
            17 -> "F__"
            18 -> "FIN"
            19 -> "FL_"
            20 -> "FR_"
            21 -> "UK_"
            22 -> "GE_"
            23 -> "GR_"
            24 -> "H__"
            25 -> "HR_"
            26 -> "I__"
            27 -> "IRL"
            28 -> "IS_"
            29 -> "KZ_"
            30 -> "L__"
            31 -> "LT_"
            32 -> "LV_"
            33 -> "M__"
            34 -> "MC_"
            35 -> "MD_"
            36 -> "MK_"
            37 -> "N__"
            38 -> "NL_"
            39 -> "P__"
            40 -> "PL_"
            41 -> "RO_"
            42 -> "RSM"
            43 -> "RUS"
            44 -> "S__"
            45 -> "SK_"
            46 -> "SLO"
            47 -> "TM_"
            48 -> "TR_"
            49 -> "UA_"
            50 -> "V__"
            51 -> "YU_"
            253 -> "EC_"
            254 -> "EUR"
            255 -> "WLD"
            else -> "UNK"
        }
    }
}