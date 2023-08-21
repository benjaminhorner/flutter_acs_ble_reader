package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.SmartCardReader
import com.benjamin.horner.flutter_acs_card_reader.Driver

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
    private lateinit var smartCardReader: SmartCardReader
    private var isScanning: Boolean = false

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_acs_card_reader")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        smartCardReader = SmartCardReader(channel)

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
                val driver = call.argument<Map<String, Any>>("driver")
                if (device != null && driver != null) {
                    readSmartCard(device, result, driver)
                } else {
                    result.error("INVALID_DEVICE", "Invalid device or driver parameter", null)
                }
            }
            "stopGattConnection" -> {
                stopGattConnection()
            }
            else -> {
                result.notImplemented()
            }
        }
    }   

    private fun readSmartCard(device: Map<String, Any>, result: Result, driverMap: Map<String, Any>) {
        val address = device["address"] as? String
        val card = driverMap["card"] as? String
        val name = driverMap["name"] as? String
        val firstName = driverMap["firstName"] as? String
        val email = driverMap["email"] as? String
        val phone = driverMap["phone"] as? String

        if (address != null && card != null && firstName != null && name != null && email != null && phone != null) {
            val driver = Driver(
                carte = card,
                nom = name,
                prenom = firstName,
                email = email,
                tel = phone
            )
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            smartCardReader.readSmartCard(bluetoothDevice, activity, context, driver)
        } else {
            result.error("INVALID_DEVICE", "Invalid device address", null)
        }
    }

    private fun stopGattConnection() {
        smartCardReader.disconnectReader()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
