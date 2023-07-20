import 'dart:async';
import 'dart:convert';

// Import
import 'package:flutter/services.dart';
import 'enums/card_connection_state.enum.dart';
import 'enums/device_connection_state.enum.dart';
import 'models/bluetooth_device.model.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'models/bluetooth_device.model.dart';
export 'enums/device_connection_state.enum.dart';

class FlutterAcsCardReader {
  static const MethodChannel _channel =
      MethodChannel('flutter_acs_card_reader');

  static Future<List<BluetoothDevice>> scanSmartCardDevices(
      {int timeoutMillis = 10000}) async {
    try {
      final List<Object?> devices =
          await _channel.invokeMethod('scanSmartCardDevices');
      final List<dynamic> json = devices
          .map(
            (value) => jsonDecode(
              jsonEncode(value),
            ),
          )
          .toList();

      final List<BluetoothDevice> bluetoothDevices =
          json.map((value) => BluetoothDevice.fromMap(value)).toList();

      return bluetoothDevices;
    } on PlatformException catch (e) {
      throw Exception('Failed to scan smart card devices: ${e.message}');
    }
  }

  static Future<void> stopScanningSmartCardDevices() async {
    try {
      await _channel.invokeMethod('stopScanningSmartCardDevices');
    } on PlatformException catch (e) {
      throw ('Failed to stop scanning for smart card devices: ${e.message}');
    }
  }

  static Future<String> readSmartCard(BluetoothDevice device) async {
    try {
      Map<String, dynamic> mappedDevice = device.toMap();
      return await _channel
          .invokeMethod('readSmartCard', {'device': mappedDevice});
    } catch (e) {
      throw Exception('Error reading smart card: $e');
    }
  }

  /// Listeners
  ///
  static Future<void> registerDeviceConnectionStateListener(
      Function(DeviceConnectionState) onEvent) async {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onDeviceConnectionEvent') {
        final dynamic eventData = call.arguments;
        switch (eventData) {
          case "STOPPED":
            onEvent(DeviceConnectionState.stopped);
            break;
          case "SEARCHING":
            onEvent(DeviceConnectionState.searching);
            break;
          default:
            onEvent(DeviceConnectionState.stopped);
            break;
        }
      }
    });
  }

  static Future<void> registerCardConnectionStateListener(
      Function(CardConnectionState) onEvent) async {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onCardConnectionEvent') {
        final dynamic eventData = call.arguments;
        switch (eventData) {
          case "CONNECTED":
            onEvent(CardConnectionState.connected);
            break;
          case "DISCONNECTED":
            onEvent(CardConnectionState.disconnected);
            break;
          case "BONDING":
            onEvent(CardConnectionState.bonding);
            break;
          default:
            onEvent(CardConnectionState.disconnected);
            break;
        }
      }
    });
  }
}
