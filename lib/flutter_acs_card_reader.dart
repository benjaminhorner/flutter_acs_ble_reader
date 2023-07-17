import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';

export 'package:flutter_blue_plus/flutter_blue_plus.dart';

class FlutterAcsCardReader {
  static const MethodChannel _channel =
      MethodChannel('flutter_acs_card_reader');
  static final FlutterBluePlus flutterBlue = FlutterBluePlus.instance;

  static Stream<List<ScanResult>> listentToScanResults() =>
      flutterBlue.scanResults;

  static Future<void> scanSmartCardDevices({int timeoutMillis = 10000}) async {
    try {
      await flutterBlue.startScan(
        timeout: Duration(milliseconds: timeoutMillis),
      );
    } catch (e) {
      throw Exception('Failed to scan smart card devices: $e');
    }
  }

  static Future<void> stopScanningSmartCardDevices() async {
    try {
      await flutterBlue.stopScan();
    } catch (e) {
      throw Exception('Failed to stop scanning for smart card devices: $e');
    }
  }

  static Future<String> readSmartCard(BluetoothDevice device) async {
    try {
      // Perform the SmartCard reading operations using the device
      // Replace the following code with the actual SmartCard reading logic
      const data = 'SmartCard data';

      return data;
    } catch (e) {
      throw Exception('Failed to read SmartCard: $e');
    }
  }
}
