package com.benjamin.horner.flutter_acs_card_reader

class HexToBytesHelper {
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
}