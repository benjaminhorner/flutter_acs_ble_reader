package com.benjamin.horner.flutter_acs_card_reader

import android.util.Log
import java.io.UnsupportedEncodingException

// ACS
import com.acs.bluetooth.BluetoothReader
import com.acs.bluetooth.Acr1255uj1Reader
import com.acs.bluetooth.Acr3901us1Reader

class SmartCardInitializer {
    val DEFAULT_3901_MASTER_KEY = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF"
    /* Get 8 bytes random number APDU. */
    val DEFAULT_3901_APDU_COMMAND = "80 84 00 00 08"
    /* Get Serial Number command (0x02) escape command. */
    val DEFAULT_3901_ESCAPE_COMMAND = "02"
    /* Default master key. */
    val DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth"
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    val DEFAULT_1255_APDU_COMMAND = "FF B0 00 04 01"
    /* Get firmware version escape command. */
    val DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 18 00"
    var FINAL_MASTER_KEY: String? = null
    var FINAL_APDU_COMMAND: String? = null
    var FINAL_ESCAPE_COMMAND: String? = null


    fun initCardReader(reader: BluetoothReader) {
       if (reader is Acr3901us1Reader) {
            /* The connected reader is ACR3901U-S1 reader. */
            if (FINAL_MASTER_KEY == null) {
                FINAL_MASTER_KEY = DEFAULT_3901_MASTER_KEY
            }
            if (FINAL_APDU_COMMAND == null) {
                FINAL_APDU_COMMAND = DEFAULT_3901_APDU_COMMAND
            }
            if (FINAL_ESCAPE_COMMAND == null) {
                FINAL_ESCAPE_COMMAND = DEFAULT_3901_ESCAPE_COMMAND
            }
        } else if (reader is Acr1255uj1Reader) {
            /* The connected reader is ACR1255U-J1 reader. */
            if (FINAL_MASTER_KEY?.length == 0) {
                try {
                    val charset = Charsets.UTF_8
                    FINAL_MASTER_KEY = toHexString(
                        DEFAULT_1255_MASTER_KEY
                            .toByteArray(charset)
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
            if (FINAL_APDU_COMMAND?.length == 0) {
                FINAL_APDU_COMMAND = DEFAULT_1255_APDU_COMMAND
            }
            if (FINAL_ESCAPE_COMMAND?.length == 0) {
                FINAL_ESCAPE_COMMAND = DEFAULT_1255_ESCAPE_COMMAND
            }
        }

        Log.e("SCAN_DEVICE", "FINAL_MASTER_KEY: $FINAL_MASTER_KEY")
    }

    private fun toHexString(buffer: ByteArray): String {
        var bufferString = ""
        for (i in buffer.indices) {
            var hexChar = Integer.toHexString(buffer[i].toInt() and 0xFF)
            if (hexChar.length == 1) {
                hexChar = "0$hexChar"
            }

            bufferString += hexChar.toUpperCase() + " "
        }
        return bufferString
    }
}