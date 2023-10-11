package com.benjamin.horner.flutter_acs_card_reader

import java.nio.charset.StandardCharsets

class HexHelper {
    fun byteArrayToHexString(buffer: ByteArray): String {
        var bufferString = ""
        for (i in buffer.indices) {
            var hexChar = (buffer[i].toInt() and 0xFF).toString(16)
                .padStart(2, '0') // Ensure two-character representation
    
            bufferString += hexChar.toUpperCase() + " "
        }
        
        return bufferString.trim() // Remove trailing space, if any
    }
    
    fun hexStringToByteArray(hexString: String): ByteArray {
        val sanitized = hexString.replace(" ", "") // Remove spaces if present
        val length = sanitized.length
        val byteArray = ByteArray(length / 2)
    
        for (i in 0 until length step 2) {
            val byte = sanitized.substring(i, i + 2).toInt(16).toByte()
            byteArray[i / 2] = byte
        }
    
        return byteArray
    }

    fun convertHexToASCII(hex: String): String {
        var hex = hex.replace(" ", "") // Remove spaces
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

            // split the hex into pairs
            val pair = hex.substring(i, i + 2)
            // convert hex to decimal
            val dec = Integer.parseInt(pair, 16)
            str = checkCode(dec)
            // ascii=ascii+" "+str
            ascii += str
            i += 2
        }
        return ascii
    }

    fun checkCode(dec: Int): String {
        var str: String

        // convert the decimal to character
        str = dec.toChar().toString()

        if (dec < 32 || dec in 127..160)
            str = "" // n/a"
        return str
    }



    fun hexStringToAscii(hexString: String): String {
        val sanitizedHex = hexString.replace(" ", "") // Remove spaces from the hex string
        val result = StringBuilder()
        for (i in 0 until sanitizedHex.length step 2) {
            val hex = sanitizedHex.substring(i, i + 2)
            val decimal = Integer.parseInt(hex, 16)
            result.append(decimal.toChar())
        }
        return result.toString()
    }        
}