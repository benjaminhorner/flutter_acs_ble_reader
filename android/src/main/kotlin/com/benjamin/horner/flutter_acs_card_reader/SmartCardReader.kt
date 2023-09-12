package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier

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

private val TAG = "SmartCardReader"
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private val deviceNotifier = DeviceNotifier()

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
        class CardTerminalScanCallback : BluetoothTerminalManager.TerminalScanCallback {
            override fun onScan(terminal: CardTerminal) {
                if (terminal.name.contains("ACR")) {
                    mManager?.stopScan()
                    deviceNotifier.updateState(terminal, channel)
                    activity.runOnUiThread {
                        Log.e(TAG, terminal.name)
                    }
                }
            }
        }
        
        val scanCallback = CardTerminalScanCallback()
        if (mManager != null) {
            Log.e(TAG, "start scanning for devices…")
            mManager!!.startScan(cardTerminalType, scanCallback)
        } else {
            Log.e(TAG, "mManager cannot be null at this point")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }
        
        /* Stop the scan. */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e(TAG, "stop scanning for devices…")
            deviceConnectionStatusNotifier.updateState("DISCONNECTED", channel)
        }, timeoutSeconds.toLong())     
    }
    
}
