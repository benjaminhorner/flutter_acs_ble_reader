package com.benjamin.horner.flutter_acs_card_reader
import com.benjamin.horner.flutter_acs_card_reader.SmartCardReader
import com.benjamin.horner.flutter_acs_card_reader.DeviceConnectionStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.BluetoothAuthStatusNotifier

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class FlutterAcsCardReaderPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var isScanning: Boolean = false
    private val discoveredDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private val smartCardReader = SmartCardReader()
    private val DEVICE_SCAN_SEARCHING: String = "SEARCHING"
    private val DEVICE_SCAN_STOPPED: String = "STOPPED"
    private val STOP_SCAN_ERROR: String = "STOP_SCAN_ERROR"
    private val SCAN_NOT_IN_PROGRESS: String = "SCAN_NOT_IN_PROGRESS"
    private val SCAN_ERROR: String = "SCAN_ERROR"
    private val BLUETOOTH_UNSUPPORTED: String = "BLUETOOTH_UNSUPPORTED"
    private val BLUETOOTH_DISABLED: String = "BLUETOOTH_DISABLED"
    private val SCAN_ALREADY_IN_PROGRESS: String = "SCAN_ALREADY_IN_PROGRESS"
    private val LOCATION_PERMISSION_GRANTED: Boolean = true
    private val LOCATION_PERMISSION_DENIED: Boolean = false
    private val deviceConnectionStatusNotifier: DeviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
    private val deviceNotifier: DeviceNotifier = DeviceNotifier()
    private val bluetoothAuthStatusNotifier: BluetoothAuthStatusNotifier = BluetoothAuthStatusNotifier()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_acs_card_reader")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext

        // Initialize Bluetooth adapter
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "scanSmartCardDevices" -> {
                scanSmartCardDevices(10000L, result)
            }
            "stopScanningSmartCardDevices" -> {
                stopScanningSmartCardDevices()
            }
            "readSmartCard" -> {
                val device = call.argument<Map<String, Any>>("device")
                if (device != null) {
                    readSmartCard(device, result)
                } else {
                    result.error("INVALID_DEVICE", "Invalid device parameter", null)
                }
            }
            "requestLocationPermission" -> {
                requestLocationPermission()
            }
            else -> {
                result.notImplemented()
            }
        }
    }   

    private fun scanSmartCardDevices(timeoutMillis: Long = 10000L, result: Result) {
        if (!isBluetoothSupported()) {
            Log.e("BLUETOOTH_UNSUPPORTED", "Bluetooth is not supported on this device")
            deviceConnectionStatusNotifier.updateState(BLUETOOTH_UNSUPPORTED, channel)
            return
        }

        if (!isBluetoothEnabled()) {
            Log.e("BLUETOOTH_DISABLED", "Bluetooth is disabled")
            deviceConnectionStatusNotifier.updateState(BLUETOOTH_DISABLED, channel)
            return
        }

        if (!isLocationPermissionGranted()) {
            // Request location permission here
            requestLocationPermission()
            return
        }

        if (isScanning) {
            Log.e("SCAN_ALREADY_IN_PROGRESS", "Bluetooth scan is already in progress")
            deviceConnectionStatusNotifier.updateState(SCAN_ALREADY_IN_PROGRESS, channel)
            return
        }

        try {
            deviceConnectionStatusNotifier.updateState(DEVICE_SCAN_SEARCHING, channel)
            isScanning = true
            discoveredDevices.clear()
            val scanCallback = object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                    if (device != null && device.name?.startsWith("ACR") == true) {
                        discoveredDevices.add(device)
                        //stopBluetoothScan()
                        //isScanning = false
                        deviceNotifier.updateState(mapDeviceToMap(device), channel)
                    }
                }
            }

            bluetoothAdapter.startLeScan(scanCallback)

            android.os.Handler().postDelayed({
                stopBluetoothScan()
                val deviceList = discoveredDevices.map { mapDeviceToMap(it) }
                deviceConnectionStatusNotifier.updateState(DEVICE_SCAN_STOPPED, channel)
                isScanning = false
            }, timeoutMillis)

        } catch (e: Exception) {
            Log.e("SCAN_ERROR", e.message ?: "Unknown error")
            deviceConnectionStatusNotifier.updateState(SCAN_ERROR, channel)
            isScanning = false
        }
    }

    private fun stopScanningSmartCardDevices() {
        if (!isScanning) {
            Log.e("SCAN_NOT_IN_PROGRESS", "Bluetooth scan is not in progress")
            deviceConnectionStatusNotifier.updateState(SCAN_NOT_IN_PROGRESS, channel)
            return
        }

        try {
            stopBluetoothScan()
            deviceConnectionStatusNotifier.updateState(DEVICE_SCAN_STOPPED, channel)
            isScanning = false
        } catch (e: Exception) {
            Log.e("STOP_SCAN_ERROR", e.message ?: "Unknown error")
            deviceConnectionStatusNotifier.updateState(STOP_SCAN_ERROR, channel)
        }
    }


    private fun readSmartCard(device: Map<String, Any>, result: Result) {
        val address = device["address"] as? String
        if (address != null) {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            val smartCardData = smartCardReader.readSmartCard(bluetoothDevice, activity, context)
            result.success(smartCardData)
        } else {
            result.error("INVALID_DEVICE", "Invalid device address", null)
        }
    }

    private fun mapDeviceToMap(device: BluetoothDevice): Map<String, Any?> {
        val deviceMap = mutableMapOf<String, Any?>()
        deviceMap["name"] = device.name
        deviceMap["address"] = device.address
        deviceMap["type"] = device.type
        deviceMap["bondState"] = device.bondState
        return deviceMap
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun stopBluetoothScan() {
        isScanning = false
        bluetoothAdapter.stopLeScan(null)
    }

    private fun isBluetoothSupported(): Boolean {
        return BluetoothAdapter.getDefaultAdapter() != null
    }

    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted()) {
            // Permission already granted, inform the Flutter side
            Log.e("LOCATION_PERMISSION_GRANTED", "Location permission is already Granted")
            deviceConnectionStatusNotifier.updateState(LOCATION_PERMISSION_GRANTED, channel)
        } else {
            // Request location permission from the Flutter side
            Log.e("LOCATION_PERMISSION_UNKNOWN", "Request location permission")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && permissions!!.size == 2 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && permissions[1] == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
            if (grantResults!![0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, background mode can be enabled
                Log.e("LOCATION_PERMISSION_GRANTED", "Location permission is Granted")
                bluetoothAuthStatusNotifier.updateState(true, channel)
            } else {
                if (!shouldShowRequestBackgroundPermissionRationale()) {
                    Log.e("LOCATION_PERMISSION_DENIED_NEVER_ASK", "Location permission denied forever - please open settings")
                    bluetoothAuthStatusNotifier.updateState(false, channel)
                } else {
                    Log.e("LOCATION_PERMISSION_DENIED", "Location permission denied")
                    bluetoothAuthStatusNotifier.updateState(false, channel)
                }
            }
        }
        return false
    }

    private fun shouldShowRequestBackgroundPermissionRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } ?: throw ActivityNotFoundException()
        } else {
            false
        }

    //override fun onRequestPermissionsResult(
    //    requestCode: Int,
    //    permissions: Array<out String>, 
    //    grantResults: IntArray,
    //) {
    //    when (requestCode) {
    //        REQUEST_LOCATION_PERMISSION -> {
    //            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //                // Location permission granted, send the result to Dart
    //                Log.e("LOCATION_PERMISSION_GRANTED", "Location permission is Granted")
    //                bluetoothAuthStatusNotifier.updateState(true, channel)
    //            } else {
    //                // Location permission denied, send the result to Dart
    //                Log.e("LOCATION_PERMISSION_DENIED", "Location permission is Denied")
    //                bluetoothAuthStatusNotifier.updateState(false, channel)
    //            }
    //        }
    //    }
    //}

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        stopBluetoothScan()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
