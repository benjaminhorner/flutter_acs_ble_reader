import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

import 'models/bluetooth_device.model.dart';

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
}
