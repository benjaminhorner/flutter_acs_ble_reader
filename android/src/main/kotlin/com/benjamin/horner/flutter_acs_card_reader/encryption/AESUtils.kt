package com.benjamin.horner.flutter_acs_card_reader

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.KeyGenerator

object AESUtils {
    fun generateKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()
        return secretKey.toString()
    }

    fun encrypt(data: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cipherBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(cipherBytes, Base64.DEFAULT)
    }

    fun decrypt(data: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        val plainBytes = cipher.doFinal(decodedBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }
}
