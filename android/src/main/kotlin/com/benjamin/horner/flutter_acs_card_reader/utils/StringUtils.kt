package com.benjamin.horner.flutter_acs_card_reader

class StringUtils {
    companion object {
        fun getHexString(sChaine: String, intSize: Int): String {
            //couper une chaine  hexa (avec espace) depuis le dÃ©but
            var sResult = sChaine
            //int iTaille=sChaine.length()

            //if (debut-1+iTaille<=iTaille){
            sResult = sResult.substring(0, intSize * 2 + intSize - 1)
            //}
            return sResult
        }
        
        fun convertHexToString(hex: String): String {
            var hex = hex
            var ascii = ""
            var str: String

            // Convert hex string to "even" length
            val rmd: Int
            val length: Int
            length = hex.length
            rmd = length % 2
            if (rmd == 1)
                hex = "0$hex"

            // split into two characters
            var i = 0
            while (i < hex.length - 1) {

                //split the hex into pairs
                val pair = hex.substring(i, i + 2)
                //convert hex to decimal
                val dec = Integer.parseInt(pair, 16)
                str = checkCode(dec)
                //ascii=ascii+" "+str
                ascii += str
                i += 2
            }
            return ascii
        }

        fun checkCode(dec: Int): String {
            var str: String

            //convert the decimal to character
            str = dec.toChar().toString()

            if (dec < 32 || dec in 127..160)
                str = "" //n/a"
            return str
        }
    }
}