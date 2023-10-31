package com.benjamin.horner.flutter_acs_card_reader

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Utils {
    fun encryptStr(): String {
        val passKey: String = "tachyphone2017"
        val dataBytes = passKey.toByteArray()
        val md5: MessageDigest
        try {
            md5 = MessageDigest.getInstance("MD5")
            md5.update(dataBytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw RuntimeException("MD5 algorithm not found", e)
        }

        val resultBytes = md5.digest()
        val sb = StringBuilder()
        for (b in resultBytes) {
            val hex = Integer.toHexString(0xFF and b.toInt())
            if (hex.length == 1) {
                sb.append("0")
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}
