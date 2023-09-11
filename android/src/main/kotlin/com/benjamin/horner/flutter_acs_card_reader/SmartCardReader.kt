package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver

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
private val SCAN_PERIOD = 10000L
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()

class SmartCardReader
    (private val channel: MethodChannel) {
    private lateinit var driver: Driver
    private lateinit var activity: Activity
    private var mManager: BluetoothTerminalManager? = null
    private var mFactory: TerminalFactory? = null
    private var mHandler: Handler = Handler()

    fun connectToDevice(activity: Activity, context: Context, driver: Driver) {
        this.activity = activity
        this.driver = driver

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
        startScan()

    }

    private fun startScan() {
        Log.e(TAG, "Scanning for devices…")
        class CardTerminalScanCallback : BluetoothTerminalManager.TerminalScanCallback {
            override fun onScan(terminal: CardTerminal) {
                activity.runOnUiThread {
                    Log.e(TAG, terminal.name)
                }
            }
        }
        
        val scanCallback = CardTerminalScanCallback()
        if (mManager != null) {
            Log.e(TAG, "start scanning for devices…")
            mManager!!.startScan(0, scanCallback)
        } else {
            Log.e(TAG, "mManager cannot be null at this point")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }
        
        /* Stop the scan. */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e(TAG, "stop scanning for devices…")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
        }, SCAN_PERIOD)     
    }
    
}
