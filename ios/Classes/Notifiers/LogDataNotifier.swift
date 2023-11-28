//
//  LogDataNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 28/11/2023.
//

import Flutter

class LogDataNotifier {
    func updateState(logData: String, channel: FlutterMethodChannel) {
        channel.invokeMethod("onReceiveLogDataEvent", arguments: logData)
    }
}

