package com.benjamin.horner.flutter_acs_card_reader

import com.sogestmatic.wrapper.Wrapper

class MathUtils {
    companion object {
        var sHexa: String = ""

        fun hexaToDecimal(hexa: String): Int {
            return Integer.parseInt(hexa.replace("\\s".toRegex(), ""), 16)
        }

        fun decimalToHexa(decimal: Int, totalBites: Int): String {
            var hexadecimal = Integer.toHexString(decimal)
            var stringSize = hexadecimal.length
            if (stringSize % 2 != 0) {
                hexadecimal = "0$hexadecimal"
            }
            stringSize = hexadecimal.length
            if (stringSize > 2) {
                var i = 0
                while (i < stringSize - 1) {
                    hexadecimal = hexadecimal.substring(0, i + 2) + " " + hexadecimal.substring(
                        i + 2,
                        stringSize
                    )
                    i += 3
                }
            }
            stringSize = hexadecimal.length
            if (stringSize < totalBites * 2) {
                hexadecimal = "00 $hexadecimal"
            }
            return hexadecimal.toUpperCase()
        }

        fun setHexa(w: Wrapper, isCmd_sign: Boolean, isEnTete: Boolean, sC1B: String) {
            val isTG2: Boolean
            var sHeader: String = ""
            var sCode: String = ""
            var sHexaSize: String = ""
            isTG2 = w.tG2
            /*Gestion de l'en-tête*/
            if (isCmd_sign) {
                sCode = if (!isTG2) " 01 " else " 03 " //[01]code TG1 [03]TG2
                // TODO Modified on the 4th of march 2020 not tested yet
                sHexaSize = if (!isTG2) MathUtils.decimalToHexa(128, 2) else MathUtils.decimalToHexa(64, 2) //[128]taille signature TG1 [64]TG2
            } else {
                sCode = if (!isTG2) " 00 " else " 02 " //[00]TG1 [02]TG2
                // TODO Modified on the 4th of march 2020 not tested yet
                sHexaSize = MathUtils.decimalToHexa(w.getTaille(), 2)
            }
            /*Construction de l'en-tête ID commande + code + taille*/
            if (isEnTete) {
                sHeader = w.commande.substring(w.commande.length - 6) + sCode + sHexaSize
            }
            /*sHexa*/
            sHexa += sHeader + sC1B
        }

        fun addHexa(hex1: String, hex2: String): String {
            val nb1 = hexaToDecimal(hex1.replace("\\s".toRegex(), ""))
            val nb2 = hexaToDecimal(hex2.replace("\\s".toRegex(), ""))
            val add = nb1 + nb2
            return decimalToHexa(add, 2)
        }
    }
}