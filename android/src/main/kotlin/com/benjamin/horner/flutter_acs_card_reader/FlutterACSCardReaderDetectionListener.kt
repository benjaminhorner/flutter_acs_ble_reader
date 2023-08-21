package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.ReaderDetectionNotifier
import com.benjamin.horner.flutter_acs_card_reader.ReaderDetectionStatus

// ACS
import com.acs.bluetooth.BluetoothReaderManager
import com.acs.bluetooth.BluetoothReader
import com.acs.bluetooth.Acr1255uj1Reader
import com.acs.bluetooth.Acr3901us1Reader

// Android
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.util.Log

// Flutter
import io.flutter.plugin.common.MethodChannel

private val readerDetectionNotifier: ReaderDetectionNotifier = ReaderDetectionNotifier()

class FlutterACSCardReaderDetectionListener(private val channel: MethodChannel) : BluetoothReaderManager.OnReaderDetectionListener {
    
    override fun onReaderDetection(reader: BluetoothReader) {
        // Called when a smart card reader device is detected
        // 'reader' is the detected Bluetooth reader device
        Log.e(TAG, "onReaderDetection => reader: $reader")
        when (reader) {
            is Acr3901us1Reader -> {
                Log.e(TAG, "Used + ACR3901U-S1Reader")
                readerDetectionNotifier.updateState(ReaderDetectionStatus.Acr3901us1Reader, channel)
            }
            is Acr1255uj1Reader -> {
                Log.e(TAG, "Used + Acr1255uj1Reader")
                //readerDetectionNotifier.updateState(ReaderDetectionStatus.Acr1255uj1Reader, channel)
            }
            else -> {
                Log.e(TAG, "Reader is neither Acr3901us1Reader or Acr1255uj1Reader")
                //readerDetectionNotifier.updateState(ReaderDetectionStatus.None, channel)
            }
        }
    }

    companion object {
        private const val TAG = "FlutterACSCardReaderDetectionListener"
    }
}
