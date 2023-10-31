package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class DataTransferNotifier {
    fun updateState(data: Map<String, Any>, channel: MethodChannel) {
        val json = JSONObject(data as Map<*, *>).toString()
        channel.invokeMethod("onReceiveDataEvent", json)
    }
}