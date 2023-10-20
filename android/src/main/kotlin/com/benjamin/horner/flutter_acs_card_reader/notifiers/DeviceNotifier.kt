package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

import javax.smartcardio.CardTerminal

class DeviceNotifier {
    fun cardTerminalToMap(cardTerminal: CardTerminal): Map<String, Any?> {
        val terminalMap = mutableMapOf<String, Any?>()
        terminalMap["name"] = cardTerminal.name
        terminalMap["isCardPresent"] = cardTerminal.isCardPresent
        return terminalMap
    }
    
    fun updateState(terminal: CardTerminal, channel: MethodChannel) {
        channel.invokeMethod("onDeviceFoundEvent", cardTerminalToMap(terminal))
    }
}