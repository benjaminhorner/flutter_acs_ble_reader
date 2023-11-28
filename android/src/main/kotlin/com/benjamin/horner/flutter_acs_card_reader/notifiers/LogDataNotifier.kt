package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class LogDataNotifier {
    fun updateState(logData: String, channel: MethodChannel) {
        channel.invokeMethod("onReceiveLogDataEvent", logData)
    }
}