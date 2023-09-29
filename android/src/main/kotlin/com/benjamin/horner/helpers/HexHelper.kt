package com.benjamin.horner.flutter_acs_card_reader

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