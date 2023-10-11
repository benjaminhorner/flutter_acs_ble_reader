package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.HexHelper
import com.benjamin.horner.flutter_acs_card_reader.CardConnectionStateNotifier
import com.benjamin.horner.flutter_acs_card_reader.ApduCommand

/// Flutter
import io.flutter.plugin.common.MethodChannel

/// ACS
import com.acs.smartcardio.BluetoothSmartCard
import com.acs.smartcardio.BluetoothTerminalManager
import com.acs.smartcardio.TerminalTimeouts
import com.acs.smartcardio.TransmitOptions

/// Android
import android.content.Context
import android.app.Activity
import android.util.Log
import android.os.Handler

/// JavaX
import javax.smartcardio.TerminalFactory
import javax.smartcardio.CardTerminal
import javax.smartcardio.CardChannel
import javax.smartcardio.CommandAPDU
import javax.smartcardio.ResponseAPDU
import javax.smartcardio.CardException
import javax.smartcardio.Card
import javax.smartcardio.ATR

private val TAG = "SmartCardReader"
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private val deviceNotifier = DeviceNotifier()
private val hexToBytesHelper = HexHelper()
private val cardConnectionStateNotifier = CardConnectionStateNotifier()

class SmartCardReader
    (private val channel: MethodChannel) {
    private lateinit var driver: Driver
    private lateinit var activity: Activity
    private var cardTerminalType: Int = 0
    private var mManager: BluetoothTerminalManager? = null
    private var mFactory: TerminalFactory? = null
    private var mHandler: Handler = Handler()

    fun connectToDevice(
        activity: Activity, 
        context: Context, 
        driver: Driver, 
        cardTerminalType: Int, 
        timeoutSeconds: Int,
        ) {
        this.activity = activity
        this.driver = driver
        this.cardTerminalType = cardTerminalType

        mManager = BluetoothSmartCard.getInstance(activity).getManager()
        if (mManager == null) {
            Log.e(TAG, "mManager cannot be null")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }

        mFactory = BluetoothSmartCard.getInstance(activity).getFactory()
        if (mFactory == null) {
            Log.e(TAG, "mFactory cannot be null")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }

        /// Start scanning for devices
        startScan(timeoutSeconds)

    }

    private fun startScan(timeoutSeconds: Int) {
        // TODO: Check if a Terminal is already connected
        // If the terminal is already connected, break and read from the connected terminal
        Log.e(TAG, "Scanning for devices…")

        if (mManager != null) {
            mManager!!.startScan(cardTerminalType, object : BluetoothTerminalManager.TerminalScanCallback {
                override fun onScan(terminal: CardTerminal) {
                    if (terminal.name.contains("ACR")) {
                        mManager?.stopScan()
                        deviceNotifier.updateState(terminal, channel)
                        deviceConnectionStatusNotifier.updateState("CONNECTED", channel)
                        activity.runOnUiThread {
                            Log.e(TAG, terminal.name)
                            connectToCard(terminal, channel)
                        }
                    }
                }
            })
        } else {
            Log.e(TAG, "mManager cannot be null at this point")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }
        
        /* Stop the scan. */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e(TAG, "stop scanning for devices…")
        }, (timeoutSeconds*1000).toLong())     
    }
    
    private fun connectToCard(terminal: CardTerminal, channel: MethodChannel) {
        /* Variables */
        var card: Card
        var cardChannel: CardChannel
        
        /* APDU Commands */
        val APDU_SELECT_BY_MF_OR_EF: String = "00 A4 02 0C 02"
        val APDU_SELECT_BY_DF: String = "00 A4 04 0C 06"

        val apduList: List<ApduCommand> = listOf(
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 3F 00",
                name = "MF",
                isEF = false
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 02",
                name = "EF_ICC",
                lengthMin = 25,
                lengthMax = 25
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 00 05",
                name = "EF_IC",
                lengthMin = 8,
                lengthMax = 8
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_DF} FF 54 41 43 48 4F",
                name = "DF_TACHOGRAPH",
                isEF = false
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_DF} FF 53 4D 52 44 54",
                name = "DF_TACHOGRAPH_G2",
                isEF = false
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 01",
                name = "EF_APP_IDENTIFICATION",
                lengthMin = 17,
                lengthMax = 17
            ),
            ApduCommand(
                selectCommand = "${APDU_SELECT_BY_MF_OR_EF} 05 20",
                name = "EF_IDENTIFICATION",
                lengthMin = 143,
                lengthMax = 143
            ),
        )

        val apduReadList: MutableList<ApduCommand> = mutableListOf()
        
        // val DF_TACHOGRAPH = "${APDU_SELECT_LENGTH_6BYTES} FF 54 41 43 48 4F"
        // val DF_TACHOGRAPH_G2 = "${APDU_SELECT_LENGTH_6BYTES} FF 53 4D 52 44 54"

        // val EF_APP_IDENTIFICATION = "${APDU_SELECT_LENGTH_2BYTES} 05 01"
        // val EF_IDENTIFICATION = "${APDU_SELECT_LENGTH_2BYTES} 05 20"
        // val EF_CARD_DOWNLOAD = "${APDU_SELECT_LENGTH_2BYTES} 05 0E"
        // val EF_DRIVING_LICENCE_INFO = "${APDU_SELECT_LENGTH_2BYTES} 05 21"
        // val EF_EVENTS_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 02"
        // val EF_FAULTS_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 03"
        // val EF_DRIVER_ACTIVITY_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 04"
        // val EF_VEHICULES_USED = "${APDU_SELECT_LENGTH_2BYTES} 05 05"
        // val EF_PLACES = "${APDU_SELECT_LENGTH_2BYTES} 05 06"
        // val EF_CURRENT_USAGE = "${APDU_SELECT_LENGTH_2BYTES} 05 07"
        // val EF_CONTROL_ACTIVITY_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 08"
        // val EF_SPECIFIC_CONDITIONS = "${APDU_SELECT_LENGTH_2BYTES} 05 22"
        // val EF_CARD_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 00"
        // val EF_CA_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 08"
        // val EF_VEHICULEUNITS_USED = "${APDU_SELECT_LENGTH_2BYTES} 05 23"
        // val EF_GNSS_PLACES = "${APDU_SELECT_LENGTH_2BYTES} 05 24"
        // val EF_CARDSIGNCERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 01"
        // val EF_LINK_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 09"

        Log.e(TAG, "Connecting to card")
        cardConnectionStateNotifier.updateState("BONDING", channel)

        try {
            card = terminal.connect("*")
            val atr: ATR = card.atr
            val atrBytes: ByteArray = atr.bytes
            val atrHex: String = hexToBytesHelper.byteArrayToHexString(atrBytes)
            Log.e(TAG, "ATR is: ${atrHex}")
            Log.e(TAG, "Established connection to card!")

            cardConnectionStateNotifier.updateState("CONNECTED", channel)

            cardChannel = card.basicChannel

            for (apdu in apduList) {
                val commandAPDU = CommandAPDU(
                    hexToBytesHelper.hexStringToByteArray(apdu.selectCommand)
                )
                val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
                val responseData: ByteArray = response.data
                val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
                val responseDataToString: String = hexToBytesHelper.convertHexToASCII(responseHex)

                val sw1: Int? = response.getSW1() // Get the SW1 part of the status word.
                val sw2: Int? = response.getSW2() // Get the SW2 part of the status word.

                if (sw1 != null && sw2 != null && sw1 == 0x90 && sw2 == 0x00) {
                    // The response indicates success (SW1 = 0x90, SW2 = 0x00).
                    // Process responseData accordingly.
                    if (apdu.isEF) {
                        performHashCommand(cardChannel)
                        read(cardChannel, apdu)
                    }
                } else if (sw1 != null && sw1 == 0x6C) {
                    val remainingBytes = sw2
                    Log.e(TAG, "Remaining bytes: $remainingBytes")
                }
                else {
                    // An error occurred. Handle the error based on the SW1 and SW2 values.
                    Log.e(TAG, "Unable to transmit APDU. sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
                    // TODO: Handle the APDU error
                    break
                }
            }
            
            card.disconnect(true)

            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            e.printStackTrace()
        }
    }

    fun read(cardChannel: CardChannel, apdu: ApduCommand) {
        try {
            var p1: String = "00"
            var readCommand = "00 B0 ${p1} 00 ${byteLength(apdu, 0)}"
            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(readCommand)
            )
            
            Log.e(TAG, "Reading APDU ${apdu.name} with command ${readCommand}")

            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            val responseDataToString: String = hexToBytesHelper.convertHexToASCII(responseHex)

            val sw1: Int? = response.getSW1() // Get the SW1 part of the status word.
            val sw2: Int? = response.getSW2() // Get the SW2 part of the status word.

            if (sw1 != null && sw2 != null && sw1 == 0x90 && sw2 == 0x00) {
                // The response indicates success (SW1 = 0x90, SW2 = 0x00).
                // Process responseData accordingly.
                Log.e(TAG, "APDU Read name was ${apdu.name}")
                Log.e(TAG, "APDU Read sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
                Log.e(TAG, "APDU Read Response data size: ${responseData.size}")
                Log.e(TAG, "APDU Read Response Hex: ${responseHex}")
                Log.e(TAG, "APDU Read response data String ${responseDataToString}")
            } else if (sw1 != null && sw1 == 0x6C) {
                val remainingBytes = sw2
                Log.e(TAG, "Remaining Read bytes: $remainingBytes")
            }
            else {
                // An error occurred. Handle the error based on the SW1 and SW2 values.
                Log.e(TAG, "Unable to Read APDU ${apdu.name} sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
                // TODO: Handle the APDU error
            }
            
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            e.printStackTrace()
            // TODO: handle error
        }
    }

    private fun byteLength(apdu: ApduCommand, offset: Int): String {
        if (apdu.lengthMin == apdu.lengthMax) {
            return padHex(Integer.toHexString(apdu.lengthMin))
        } else {
            // TODO: offset by remaining bytes
            return padHex(Integer.toHexString(apdu.lengthMin))
        }
    }

    private fun padHex(hex: String): String {
        return if (hex.length == 1) {
            "0$hex"
        } else {
            hex
        }
    }

    private fun performHashCommand(cardChannel: CardChannel) {
        val HASH_COMMAND: String = "80 2A 90 00"

        try {
            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(HASH_COMMAND)
            )
            
            Log.e(TAG, "Perform hash command")

            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)

            val sw1: Int? = response.getSW1() // Get the SW1 part of the status word.
            val sw2: Int? = response.getSW2() // Get the SW2 part of the status word.

            if (sw1 != null && sw2 != null && sw1 == 0x90 && sw2 == 0x00) {
                // The response indicates success (SW1 = 0x90, SW2 = 0x00).
                // Process responseData accordingly.
                Log.e(TAG, "Hash command sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
            } else if (sw1 != null && sw1 == 0x6C) {
                val remainingBytes = sw2
                Log.e(TAG, "Remaining Read bytes: $remainingBytes")
            }
            else {
                // An error occurred. Handle the error based on the SW1 and SW2 values.
                Log.e(TAG, "Unable to make hash command sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
                // TODO: Handle the APDU error
            }
            
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            e.printStackTrace()
            // TODO: handle error
        }
    }

    private fun performSign() {
        val TG1_SIGNATURE = "00 2A 9E 9A 80" // 128 bytes
        var TG2_SIGNATURE: String? = null // 64…132 bytes
    }
}
