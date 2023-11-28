//
//  CardConnectionStateNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import Flutter

class CardConnectionStateNotifier {
    func updateState(state: String, channel: FlutterMethodChannel) {
        channel.invokeMethod("onCardConnectionEvent", arguments: state)
    }
}
