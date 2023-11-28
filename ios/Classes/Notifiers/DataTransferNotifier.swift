//
//  DataTransferNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import Flutter

class DataTransferNotifier {
    func updateState(data: [String: Any], channel: FlutterMethodChannel) {
        if let jsonData = try? JSONSerialization.data(withJSONObject: data),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            channel.invokeMethod("onReceiveDataEvent", arguments: jsonString)
        }
    }
}
