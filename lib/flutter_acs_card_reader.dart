import 'dart:async';
import 'dart:io';

// Import
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_acs_card_reader/enums/bluetooth_support.enum.dart';
import 'enums/device_connection_state.enum.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'enums/device_connection_state.enum.dart';
export 'package:flutter_blue_plus/flutter_blue_plus.dart';
export 'package:flutter_acs_card_reader/enums/bluetooth_support.enum.dart';

class FlutterAcsCardReader {
  static const MethodChannel _channel =
      MethodChannel('flutter_acs_card_reader');

  static StreamSubscription<List<ScanResult>>? scanResultsSubscription;

  // Stream controllers for each type of event
  static final StreamController<DeviceConnectionState>
      _deviceConnectionStateController =
      StreamController<DeviceConnectionState>.broadcast();
  static final StreamController<BluetoothDevice> _deviceFoundEventController =
      StreamController<BluetoothDevice>.broadcast();
  static final StreamController<BluetoothStatus> _bluetoothStatusController =
      StreamController<BluetoothStatus>.broadcast();

  // Streams to expose for listening to events
  static Stream<DeviceConnectionState> get deviceConnectionStateStream =>
      _deviceConnectionStateController.stream;
  static Stream<BluetoothDevice> get deviceFoundEventStream =>
      _deviceFoundEventController.stream;
  static Stream<BluetoothStatus> get bluetoothStatusStream =>
      _bluetoothStatusController.stream;

  /// Public
  ///
  static Future<void> scanSmartCardDevices({int timeoutSeconds = 10}) async {
    try {
      _deviceConnectionStateController.add(DeviceConnectionState.searching);
      await _scanForDevices(timeoutSeconds);
    } on PlatformException catch (e) {
      throw Exception('Failed to scan smart card devices: ${e.message}');
    }
  }

  static Future<void> stopScanningSmartCardDevices() async {
    try {
      await FlutterBluePlus.stopScan();
      _stopListeningToScanResults();
      _deviceConnectionStateController.add(DeviceConnectionState.stopped);
    } on PlatformException catch (e) {
      throw ('Failed to stop scanning for smart card devices: ${e.message}');
    }
  }

  //static Future<String> readSmartCard(BluetoothDevice device) async {
  //  try {
  //    Map<String, dynamic> mappedDevice = device.toMap();
  //    return await _channel
  //        .invokeMethod('readSmartCard', {'device': mappedDevice});
  //  } catch (e) {
  //    throw Exception('Error reading smart card: $e');
  //  }
  //}

  /// Start Streams
  ///
  static void startListeningToEvents() {
    _channel.setMethodCallHandler((MethodCall call) async {
      //if (call.method == 'onDeviceFoundEvent') {
      //  try {
      //    final dynamic device = call.arguments;
      //    debugPrint("onDeviceFoundEvent is $device");
      //    _deviceFoundEventController.add(device);
      //  } catch (e) {
      //    rethrow;
      //  }
      //}
    });
  }

  /// Stop Streams
  static Future<void> stopListeningToLocationIsGrantedEvents() async {
    await _bluetoothStatusController.close();
  }

  static Future<void> _stopListeningToScanResults() async {
    await scanResultsSubscription?.cancel();
  }

  /// Private
  ///
  static Future<void> _enableBluetooth() async {
    // check availability
    if (await FlutterBluePlus.isAvailable == false) {
      debugPrint("Bluetooth is not supported by this device");
      _bluetoothStatusController.add(BluetoothStatus.notSupported);
      return;
    }

    // turn on bluetooth ourself if we can
    if (Platform.isAndroid) {
      await FlutterBluePlus.turnOn();
    }

    // wait for bluetooth to be on
    await FlutterBluePlus.adapterState
        .where((s) => s == BluetoothAdapterState.on)
        .first;
    _bluetoothStatusController.add(BluetoothStatus.granted);
  }

  static Future<void> _scanForDevices(int timeoutSeconds) async {
    scanResultsSubscription =
        FlutterBluePlus.scanResults.listen((results) async {
      for (ScanResult r in results) {
        if (r.device.localName.startsWith("ACR")) {
          _deviceFoundEventController.add(r.device);
          await stopScanningSmartCardDevices();
        }
        debugPrint('${r.device.localName} found! rssi: ${r.rssi}');
      }
    });
    FlutterBluePlus.startScan(timeout: Duration(seconds: timeoutSeconds));
  }
}
