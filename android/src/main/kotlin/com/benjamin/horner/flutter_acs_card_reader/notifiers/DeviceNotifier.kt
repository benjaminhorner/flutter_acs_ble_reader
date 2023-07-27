package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class DeviceNotifier {
    fun updateState(device: Map<String, Any?>, channel: MethodChannel) {
        channel.invokeMethod("onDeviceFoundEvent", device)
    }
}