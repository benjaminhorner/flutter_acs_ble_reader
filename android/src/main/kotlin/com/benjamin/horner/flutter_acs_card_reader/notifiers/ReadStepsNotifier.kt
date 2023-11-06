package com.benjamin.horner.flutter_acs_card_reader

import io.flutter.plugin.common.MethodChannel
import android.util.Log

class TotalReadStepsStatusNotifier {
    fun updateState(steps: Int, channel: MethodChannel) {
        Log.e("TotalReadStepsStatusNotifier", "$steps")
        channel.invokeMethod("onUpdateTotalReadStepsEvent", steps)
    }
}

class CurrentReadStepStatusNotifier {
    fun updateState(step: Int, channel: MethodChannel) {
        Log.e("CurrentReadStepStatusNotifier", "$step")
        channel.invokeMethod("onUpdateCurrentReadStepEvent", step)
    }
}