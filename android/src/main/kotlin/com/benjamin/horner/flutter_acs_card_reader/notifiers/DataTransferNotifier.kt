package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class DataTransferNotifier {
    fun updateState(data: String, channel: MethodChannel) {
        channel.invokeMethod("onReceiveDataEvent", data)
    }
}