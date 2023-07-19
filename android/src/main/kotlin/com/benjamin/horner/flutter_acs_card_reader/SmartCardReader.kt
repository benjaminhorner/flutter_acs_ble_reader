package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.SmartCardInitializer

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.util.Log
import android.content.Context
import android.app.Activity
import android.bluetooth.BluetoothManager

// ACS
import com.acs.smartcard.Reader
import com.acs.bluetooth.BluetoothReader
import com.acs.bluetooth.BluetoothReaderManager
import com.acs.bluetooth.BluetoothReaderGattCallback
import com.acs.bluetooth.Acr1255uj1Reader
import com.acs.bluetooth.Acr3901us1Reader

private val smartCardInitializer = SmartCardInitializer()

class SmartCardReader {
    private var mBluetoothReader: BluetoothReader? = null
    private var mBluetoothReaderManager: BluetoothReaderManager? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattCallback: BluetoothReaderGattCallback? = null
    private var isFinished: Boolean = false

    fun readSmartCard(device: BluetoothDevice, activity: Activity, context: Context): String {
        mGattCallback = BluetoothReaderGattCallback()
        mBluetoothReaderManager = BluetoothReaderManager()
        mBluetoothReaderManager?.setOnReaderDetectionListener { reader ->

            smartCardInitializer.initCardReader(reader)
            when (reader) {
                is Acr3901us1Reader -> {
                    Log.e("DeviceFound", "Used + ACR3901U-S1Reader")
                }
                is Acr1255uj1Reader -> {
                    Log.e("DeviceFound", "Used + Acr1255uj1Reader")
                }
                else -> {
                    disconnectReader()
                }
            }
            if (!isFinished) {
                mBluetoothReader = reader
                //setListener()
                activateReader()
            }
        }

        connectReader(device, activity)

        val data = "SmartCard data"

        return data
    }

     fun activateReader() {
        if (mBluetoothReader == null) {
            return
        }
        if (mBluetoothReader is Acr3901us1Reader) {
            /* Start pairing to the reader. */

            Log.e("mBluetoothReader", "Binding")
            (mBluetoothReader as Acr3901us1Reader).startBonding()
        } else if (mBluetoothReader is Acr1255uj1Reader) {

            Log.e("mBluetoothReader", "Notification")
            (mBluetoothReader as? Acr1255uj1Reader)?.enableNotification(true)
        }
    }

    private fun disconnectReader() {
        if (mBluetoothGatt == null) {
            //bLectureCard = false
            //isReading = false
            return
        }
        mBluetoothGatt?.disconnect()
        //bLectureCard = false
        //isReading = false
    }

    private fun connectReader(device: BluetoothDevice, activity: Activity) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (bluetoothManager == null) {
            //UpdateConnectionState(BluetoothReader.STATE_DISCONNECTED)
            //UpdateConnectionState(BluetoothReader.STATE_DISCONNECTED)
            return //false
        }
        val bluetoothAdapter = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            //UpdateConnectionState(BluetoothReader.STATE_DISCONNECTED)
            return //false
        }
        
        if (mBluetoothGatt != null) {
            Log.e("mBluetoothGatt", "Clear old GATT connection")
            mBluetoothGatt?.disconnect()
            mBluetoothGatt?.close()
            mBluetoothGatt = null
        }
        val device = bluetoothAdapter.getRemoteDevice(device.address)
        if (device == null) {
            Log.w("device", "Device not found. Unable to connect.")
            return // false
        }
        mBluetoothGatt = device.connectGatt(activity.applicationContext, false, mGattCallback)

        return //true
    }
}
