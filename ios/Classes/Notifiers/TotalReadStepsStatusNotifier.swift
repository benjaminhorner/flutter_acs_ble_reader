//
//  TotalReadStepsStatusNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import Flutter

class TotalReadStepsStatusNotifier {
    func updateState(steps: Int, channel: FlutterMethodChannel) {
        print("TotalReadStepsStatusNotifier \(steps)")
        channel.invokeMethod("onUpdateTotalReadStepsEvent", arguments: steps)
    }
}
