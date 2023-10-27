package com.benjamin.horner.flutter_acs_card_reader

import java.nio.charset.StandardCharsets
import android.util.Log

class HexHelper {
    private val TAG = "HexHelper"

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

    fun byteLength(apdu: ApduCommand? = null, length: Int = 0): String {
        if (apdu == null && length > 0) {
            return padHex(Integer.toHexString(length))
        } else if (apdu!!.lengthMin == apdu!!.lengthMax && apdu!!.lengthMax <= 255) {
            return padHex(Integer.toHexString(apdu!!.lengthMin)).toUpperCase()
        } else if (length > 0) {
            return padHex(Integer.toHexString(length)).toUpperCase()
        } else {
            return padHex(Integer.toHexString(255)).toUpperCase()
        }
    }

    fun calculateLengthToHex(hexString: String): String {
        val length: Int = hexString.length/2
        val hex: String = padHex(Integer.toHexString(length)).toUpperCase()

        if (hex.length > 2) {
            return hex
        } else {
            val paddedHex = padHex("00$hex")
            return paddedHex
        }
    }

    private fun padHex(hex: String): String {
        val paddedHex = if (hex.length % 2 == 1) {
            "0$hex"
        } else {
            hex
        }

        val spacedHex = buildString {
            for (i in 0 until paddedHex.length step 2) {
                if (i > 0) {
                    append(' ') // Add a space after every 2 characters
                }
                append(paddedHex.substring(i, i + 2))
            }
        }

        return spacedHex
    }
 
}