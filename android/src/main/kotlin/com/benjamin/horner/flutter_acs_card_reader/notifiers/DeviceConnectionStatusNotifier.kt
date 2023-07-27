package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class DeviceConnectionStatusNotifier {
    fun updateState(state: Any, channel: MethodChannel) {
        channel.invokeMethod("onDeviceConnectionStatusEvent", state)
    }
}