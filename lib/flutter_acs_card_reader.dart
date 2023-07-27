import 'dart:async';

// Import
import 'package:flutter/services.dart';
import 'enums/device_connection_state.enum.dart';
import 'models/bluetooth_device.model.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'models/bluetooth_device.model.dart';
export 'enums/device_connection_state.enum.dart';

class FlutterAcsCardReader {
  static const MethodChannel _channel =
      MethodChannel('flutter_acs_card_reader');

  static Future<void> scanSmartCardDevices({int timeoutMillis = 10000}) async {
    try {
      await _channel.invokeMethod('scanSmartCardDevices');
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
  static Future<void> registerDeviceConnectionStatusEventListener(
      Function(DeviceConnectionState) onEvent) async {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onDeviceConnectionStatusEvent') {
        final dynamic eventData = call.arguments;
        switch (eventData) {
          case "SEARCHING":
            onEvent(DeviceConnectionState.searching);
            break;
          case "STOPPED":
            onEvent(DeviceConnectionState.stopped);
            break;
          default:
            onEvent(DeviceConnectionState.error);
        }
      }
    });
  }
}
