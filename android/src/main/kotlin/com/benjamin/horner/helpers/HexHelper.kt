package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.StringHelper
import java.nio.charset.StandardCharsets
import android.util.Log

class HexHelper {
    private val TAG = "HexHelper"
    private val stringHelper = StringHelper()

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

    fun cleanupHexString(hexString: String): String {
        return stringHelper.removeWhitespaces(hexString)
    }

    fun byteLength(apdu: ApduCommand? = null, length: Int = 0): String {
        if (apdu == null && length > 0) {
            return padHex(Integer.toHexString(length)).toUpperCase()
        } else if (apdu!!.lengthMin == apdu!!.lengthMax && apdu!!.lengthMax <= 255) {
            return padHex(Integer.toHexString(apdu!!.lengthMin)).toUpperCase()
        } else if (length > 0) {
            return padHex(Integer.toHexString(length)).toUpperCase()
        } else {
            return padHex(Integer.toHexString(255)).toUpperCase()
        }
    }

    fun calculateLengthOfHex(hexString: String): String {
        val length: Int = cleanupHexString(hexString).length/2
        return padHex(hexString = Integer.toHexString(length), desiredLength = 4).toUpperCase()
    }

    fun calculateLengthToHex(length: Int): String {
        return padHex(hexString = Integer.toHexString(length), desiredLength = 4).toUpperCase()
    }

    fun padHex(hexString: String, desiredLength: Int = 2): String {
        val paddedHex = String.format("%${desiredLength}s", hexString).replace(' ', '0')

        val spacedHex = buildString {
            for (i in 0 until paddedHex.length step 2) {
                if (i > 0) {
                    append(' ') // Add a space after every 2 characters
                }
                append(paddedHex.substring(i, i + 2))
            }
        }

        return spacedHex.toUpperCase()
    }

    // private fun padHex(hex: String): String {
    //     val paddedHex = if (hex.length % 2 == 1) {
    //         "0$hex"
    //     } else {
    //         hex
    //     }

    //     val spacedHex = buildString {
    //         for (i in 0 until paddedHex.length step 2) {
    //             if (i > 0) {
    //                 append(' ') // Add a space after every 2 characters
    //             }
    //             append(paddedHex.substring(i, i + 2))
    //         }
    //     }

    //     return spacedHex
    // }
 
}