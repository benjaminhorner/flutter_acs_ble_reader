//
//  CurrentReadStepStatusNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import Flutter

class CurrentReadStepStatusNotifier {
    func updateState(step: Int, channel: FlutterMethodChannel) {
        print("CurrentReadStepStatusNotifier \(step)")
        channel.invokeMethod("onUpdateCurrentReadStepEvent", arguments: step)
    }
}
