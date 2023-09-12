package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.HexToBytesHelper
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

private val TAG = "SmartCardReader"
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private val deviceNotifier = DeviceNotifier()
private val hexToBytesHelper = HexToBytesHelper()
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
        val hexString = "00 A4 02 0C 02 05 05"
        val byteArray = hexToBytesHelper.hexStringToByteArray(hexString)

        Log.e(TAG, "Connecting to card")
        cardConnectionStateNotifier.updateState("BONDING", channel)

        try {
            card = terminal.connect("*")
            Log.e(TAG, "Established connection to card!")
            cardConnectionStateNotifier.updateState("CONNECTED", channel)
            cardChannel = card.basicChannel
            val commandAPDU = CommandAPDU(byteArray)
            val responseAPDU: ResponseAPDU = cardChannel.transmit(commandAPDU)
            Log.e(TAG, "Starting APDU Command")
            println("${TAG}: ${responseAPDU.bytes.joinToString { it.toString(16).padStart(2, '0') }}")
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
            e.printStackTrace()
        }
    }
}
