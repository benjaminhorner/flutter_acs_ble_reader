package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class DeviceConnectionStateNotifier {
    fun updateState(state: String, channel: MethodChannel) {
        channel.invokeMethod("onDeviceConnectionEvent", state)
    }
}