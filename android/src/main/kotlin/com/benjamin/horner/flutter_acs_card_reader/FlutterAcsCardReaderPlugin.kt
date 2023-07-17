package com.benjamin.horner.flutter_acs_card_reader

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FlutterAcsCardReaderPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "flutter_acs_card_reader")
        channel.setMethodCallHandler(this)
        context = binding.applicationContext
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
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
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device["address"] as String)
        // Perform the SmartCard reading operations using the bluetoothDevice

        // Replace the following code with the actual SmartCard reading logic
        val data = "SmartCard data"

        result.success(data)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
