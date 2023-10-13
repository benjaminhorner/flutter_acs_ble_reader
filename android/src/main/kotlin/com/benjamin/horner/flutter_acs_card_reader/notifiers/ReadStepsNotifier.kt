package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel

class TotalReadStepsStatusNotifier {
    fun updateState(steps: Int, channel: MethodChannel) {
        channel.invokeMethod("onUpdateTotalReadStepsEvent", steps)
    }
}

class CurrentReadStepStatusNotifier {
    fun updateState(step: Int, channel: MethodChannel) {
        channel.invokeMethod("onUpdateCurrentReadStepEvent", step)
    }
}