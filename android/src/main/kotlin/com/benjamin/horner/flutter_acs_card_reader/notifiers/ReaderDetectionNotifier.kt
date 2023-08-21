package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.ReaderDetectionStatus
import io.flutter.plugin.common.MethodChannel

class ReaderDetectionNotifier {
    fun updateState(status: ReaderDetectionStatus, channel: MethodChannel) {
        channel.invokeMethod("onReaderDetectionStatusEvent", status)
    }
}