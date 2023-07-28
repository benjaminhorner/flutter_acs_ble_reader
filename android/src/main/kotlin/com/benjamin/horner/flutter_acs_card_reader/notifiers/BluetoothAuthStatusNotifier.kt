package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class BluetoothAuthStatusNotifier {
    fun updateState(granted: Boolean, channel: MethodChannel) {
        channel.invokeMethod("onLocationPermissionResult", granted)
    }
}