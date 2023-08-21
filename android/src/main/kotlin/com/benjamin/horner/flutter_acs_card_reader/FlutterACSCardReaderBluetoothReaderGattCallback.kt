package com.benjamin.horner.flutter_acs_card_reader

// Plugin
import com.benjamin.horner.flutter_acs_card_reader.DeviceConnectionStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.FlutterACSCardReaderDetectionListener

// Flutter
import io.flutter.plugin.common.MethodChannel

// ANdroid
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.app.Activity
import android.util.Log

// ACS
import com.acs.bluetooth.*
import com.acs.smartcard.Reader

private val deviceConnectionStateNotifier: DeviceConnectionStatusNotifier = DeviceConnectionStatusNotifier();
private val bluetoothReaderManager = BluetoothReaderManager()

class FlutterACSCardReaderBluetoothReaderGattCallback(
    private val context: Context,
    private val channel: MethodChannel,
    private val activity: Activity
    ) : BluetoothReaderGattCallback() {

    val readerDetectionListener = FlutterACSCardReaderDetectionListener(channel)

    fun setReaderDetectionListener() {
        bluetoothReaderManager.setOnReaderDetectionListener(readerDetectionListener)
    }

    override fun onConnectionStateChange(
        gatt: BluetoothGatt?,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(gatt, status, newState)

        Log.d(TAG, "onConnectionStateChange: $newState")

        activity.runOnUiThread {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    // Device connected
                    val connectedDevice = gatt?.device
                    if (connectedDevice != null) {
                        val deviceAddress = connectedDevice.address
                        val deviceName = connectedDevice.name
                        Log.d(TAG, "Device connected: $deviceName ($deviceAddress)")
                        bluetoothReaderManager.detectReader(gatt, this)
                    }
                    deviceConnectionStateNotifier.updateState("CONNECTED", channel)
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    // Device disconnected
                    val disconnectedDevice = gatt?.device
                    if (disconnectedDevice != null) {
                        val deviceAddress = disconnectedDevice.address
                        val deviceName = disconnectedDevice.name
                        Log.d(TAG, "Device disconnected: $deviceName ($deviceAddress)")
                    }
                    deviceConnectionStateNotifier.updateState("DISCONNECTED", channel)
                }
                // Handle other connection states if needed
            }
        }
    }

    // Other overridden methods to handle GATT events
    // ...

    companion object {
        private const val TAG = "FlutterACSCardReaderBluetoothReaderGattCallback"
    }
}
