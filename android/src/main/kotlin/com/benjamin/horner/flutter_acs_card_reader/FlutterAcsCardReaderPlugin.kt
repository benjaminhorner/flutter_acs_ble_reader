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
                if (driver != null) {
                    connectToDevice(result, driver)
                } else {
                    result.error("INVALID_DEVICE", "Invalid driver parameter", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }   

    private fun connectToDevice(result: Result, driverMap: Map<String, Any>) {
        val card = driverMap["card"] as? String
        val name = driverMap["name"] as? String
        val firstName = driverMap["firstName"] as? String
        val email = driverMap["email"] as? String
        val phone = driverMap["phone"] as? String

        if (card != null && firstName != null && name != null && email != null && phone != null) {
            val driver = Driver(
                carte = card,
                nom = name,
                prenom = firstName,
                email = email,
                tel = phone
            )
            smartCardReader.connectToDevice(activity, context, driver)
        } else {
            result.error("INVALID_DEVICE", "Invalid device address", null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
