package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.HexHelper
import com.benjamin.horner.flutter_acs_card_reader.CardConnectionStateNotifier

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
        val APDU_SELECT_LENGTH_2BYTES = "00 A4 02 0C 02"
        val APDU_SELECT_LENGTH_6BYTES = "00 A4 04 0C 06"
        val MF = "${APDU_SELECT_LENGTH_2BYTES} 3F 00"
        val EF_ICC = "${APDU_SELECT_LENGTH_2BYTES} 00 02"
        val EF_IC = "${APDU_SELECT_LENGTH_2BYTES} 00 05"
        val DF_TACHOGRAPH = "${APDU_SELECT_LENGTH_6BYTES} FF 54 41 43 48 4F"
        val EF_APP_IDENTIFICATION = "${APDU_SELECT_LENGTH_2BYTES} 05 01"
        val EF_IDENTIFICATION = "${APDU_SELECT_LENGTH_2BYTES} 05 20"
        val EF_CARD_DOWNLOAD = "${APDU_SELECT_LENGTH_2BYTES} 05 0E"
        val EF_DRIVING_LICENCE_INFO = "${APDU_SELECT_LENGTH_2BYTES} 05 21"
        val EF_EVENTS_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 02"
        val EF_FAULTS_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 03"
        val EF_DRIVER_ACTIVITY_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 04"
        val EF_VEHICULES_USED = "${APDU_SELECT_LENGTH_2BYTES} 05 05"
        val EF_PLACES = "${APDU_SELECT_LENGTH_2BYTES} 05 06"
        val EF_CURRENT_USAGE = "${APDU_SELECT_LENGTH_2BYTES} 05 07"
        val EF_CONTROL_ACTIVITY_DATA = "${APDU_SELECT_LENGTH_2BYTES} 05 08"
        val EF_SPECIFIC_CONDITIONS = "${APDU_SELECT_LENGTH_2BYTES} 05 22"
        val EF_CARD_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 00"
        val EF_CA_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 08"
        val DF_TACHOGRAPH_G2 = "${APDU_SELECT_LENGTH_6BYTES} FF 53 4D 52 44 54"
        val EF_VEHICULEUNITS_USED = "${APDU_SELECT_LENGTH_2BYTES} 05 23"
        val EF_GNSS_PLACES = "${APDU_SELECT_LENGTH_2BYTES} 05 24"
        val EF_CARDSIGNCERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 01"
        val EF_LINK_CERTIFICATE = "${APDU_SELECT_LENGTH_2BYTES} C1 09"

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
            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(EF_ICC)
            )
            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            val responseString: String = hexToBytesHelper.hexStringToAscii(responseHex)

            val sw1: Int? = response.getSW1() // Get the SW1 part of the status word.
            val sw2: Int? = response.getSW2() // Get the SW2 part of the status word.

            if (sw1 != null && sw2 != null && sw1 == 0x90 && sw2 == 0x00) {
                // The response indicates success (SW1 = 0x90, SW2 = 0x00).
                // Process responseData accordingly.
                Log.e(TAG, "APDU sw1 was ${sw1} and sw2 was ${sw2}")
                Log.e(TAG, "APDU Response data to String: ${responseString}")
            } else {
                // An error occurred. Handle the error based on the SW1 and SW2 values.
                Log.e(TAG, "Unable to transmit APDU. sw1 was ${sw1} and sw2 was ${sw2}")
                // TODO: Handle the APDU error
            }
            
            
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            e.printStackTrace()
        }
    }
}
