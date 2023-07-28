package com.benjamin.horner.flutter_acs_card_reader
import com.benjamin.horner.flutter_acs_card_reader.SmartCardReader
import com.benjamin.horner.flutter_acs_card_reader.DeviceConnectionStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.BluetoothAuthStatusNotifier

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
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
    private val smartCardReader = SmartCardReader()

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
            "readSmartCard" -> {
                val device = call.argument<Map<String, Any>>("device")
                if (device != null) {
                    readSmartCard(device, result)
                } else {
                    result.error("INVALID_DEVICE", "Invalid device parameter", null)
                }
            }
            else -> {
                result.notImplemented()
            }
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

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
