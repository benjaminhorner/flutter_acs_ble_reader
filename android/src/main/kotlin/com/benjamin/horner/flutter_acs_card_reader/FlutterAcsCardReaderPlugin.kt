package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.SmartCardReader
import com.benjamin.horner.flutter_acs_card_reader.Driver

/// Android
import android.content.Context
import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull

/// Flutter 
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
    private lateinit var smartCardReader: SmartCardReader
    private var isScanning: Boolean = false

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_acs_card_reader")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        smartCardReader = SmartCardReader(channel)
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
            "connectToDevice" -> {
                val driver = call.argument<Map<String, Any>>("driver")
                val cardTerminalType = call.argument<Int>("cardTerminalType")
                val timeoutSeconds = call.argument<Int>("timeoutSeconds")
                if (driver != null && cardTerminalType != null && timeoutSeconds != null) {
                    connectToDevice(result, driver, cardTerminalType, timeoutSeconds)
                } else {
                    result.error("INVALID_DEVICE", "Invalid driver, cardTerminalType or timeoutSeconds parameter", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }   

    private fun connectToDevice(
        result: Result, 
        driverMap: Map<String, Any>,
        cardTerminalType: Int, 
        timeoutSeconds: Int,
        ) {
        val card = driverMap["card"] as? String
        val name = driverMap["name"] as? String
        val firstName = driverMap["firstName"] as? String
        val email = driverMap["email"] as? String
        val phone = driverMap["phone"] as? String
        val agencyID = driverMap["agencyID"] as? String

        if (card != null && firstName != null && name != null && email != null && phone != null && agencyID != null) {
            val driver = Driver(
                card = card,
                name = name,
                firstName = firstName,
                email = email,
                phone = phone,
                agencyID = agencyID
            )
            smartCardReader.connectToDevice(activity, context, driver, cardTerminalType, timeoutSeconds)
        } else {
            result.error("INVALID_DEVICE", "Invalid parameters $agencyID", null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
