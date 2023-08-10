package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class CardConnectionStateNotifier {
    fun updateState(state: Int, channel: MethodChannel) {
        channel.invokeMethod("onCardConnectionEvent", state)
    }
}