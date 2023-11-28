//
//  DeviceNotifier.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 24/11/2023.
//

import Flutter
import SmartCardIO

class DeviceNotifier {
    func cardTerminalToMap(cardTerminal: CardTerminal) throws -> [String: Any?] {
        do {
            var terminalMap = [String: Any?]()
            terminalMap["name"] = cardTerminal.name
            terminalMap["isCardPresent"] = try cardTerminal.isCardPresent()
            return terminalMap
        } catch {
            throw error
        }
    }

    func updateState(terminal: CardTerminal, channel: FlutterMethodChannel) throws {
        do {
            let terminalMap: [String: Any?] = try cardTerminalToMap(cardTerminal: terminal)
            channel.invokeMethod("onDeviceFoundEvent", arguments: terminalMap)
        } catch {
            throw error
        }
    }
}
