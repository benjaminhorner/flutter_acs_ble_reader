import 'dart:async';

// Import
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_acs_card_reader/enums/bluetooth_support.enum.dart';
import 'package:flutter_acs_card_reader/enums/device_connection_state.enum.dart';
import 'enums/device_search_state.enum.dart';
import 'models/user.model.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'enums/device_search_state.enum.dart';
export 'package:flutter_blue_plus/flutter_blue_plus.dart';
export 'package:flutter_acs_card_reader/enums/bluetooth_support.enum.dart';
export 'package:flutter_acs_card_reader/enums/device_connection_state.enum.dart';
export 'models/user.model.dart';

class FlutterAcsCardReader {
  static const MethodChannel _channel =
      MethodChannel('flutter_acs_card_reader');

  static StreamSubscription<List<ScanResult>>? scanResultsSubscription;

  // Stream controllers for each type of event
  static final StreamController<DeviceSearchState>
      _deviceSearchStateController =
      StreamController<DeviceSearchState>.broadcast();
  static final StreamController<DeviceConnectionState>
      _deviceConnectionStateController =
      StreamController<DeviceConnectionState>.broadcast();
  static final StreamController<BluetoothDevice> _deviceFoundEventController =
      StreamController<BluetoothDevice>.broadcast();
  static final StreamController<BluetoothStatus> _bluetoothStatusController =
      StreamController<BluetoothStatus>.broadcast();

  // Streams to expose for listening to events
  static Stream<DeviceSearchState> get deviceSearchStateStream =>
      _deviceSearchStateController.stream;
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
      _deviceSearchStateController.add(DeviceSearchState.searching);
      await _scanForDevices(timeoutSeconds);
    } on PlatformException catch (e) {
      throw Exception('Failed to scan smart card devices: ${e.message}');
    }
  }

  static Future<void> stopScanningSmartCardDevices() async {
    try {
      await FlutterBluePlus.stopScan();
      _stopListeningToScanResults();
      _deviceSearchStateController.add(DeviceSearchState.stopped);
    } on PlatformException catch (e) {
      throw ('Failed to stop scanning for smart card devices: ${e.message}');
    }
  }

  static Future<void> readSmartCard(BluetoothDevice device,
      {required User user}) async {
    try {
      Map<String, dynamic> mappedDevice = _deviceToMap(device);
      Map<String, dynamic> mappedUser = _userToMap(user);
      await _channel.invokeMethod(
        'readSmartCard',
        {'device': mappedDevice, "driver": mappedUser},
      );
    } catch (e) {
      throw Exception('Error reading smart card: $e');
    }
  }

  /// Start Streams
  ///
  static void startListeningToEvents() {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onDeviceConnectionStatusEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onDeviceConnectionStatusEvent is $state");
          _deviceConnectionStateController.add(_deviceConnectionState(state));
        } catch (e) {
          rethrow;
        }
      } else if (call.method == 'onDeviceConnectionStatusEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onReaderDetectionStatusEvent is $state");
          _deviceConnectionStateController.add(_deviceConnectionState(state));
        } catch (e) {
          rethrow;
        }
      }
    });
  }

  /// Stop Streams
  static Future<void> stopListeningToLocationIsGrantedEvents() async {
    await _bluetoothStatusController.close();
  }

  static Future<void> _stopListeningToScanResults() async {
    await scanResultsSubscription?.cancel();
  }

  static Future<void> _stopListeningToDeviceConnectionResults() async {
    await scanResultsSubscription?.cancel();
  }

  /// Private
  ///
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

  static Future<void> _stopGattConnection() async {
    try {
      await _channel.invokeMethod('stopGattConnection');
    } catch (e) {
      throw Exception('Error stopping GATT Connection: $e');
    }
  }

  static Map<String, dynamic> _deviceToMap(BluetoothDevice device) {
    return {
      'name': device.localName,
      'address': device.remoteId.str,
      'type': device.type.index,
    };
  }

  static Map<String, dynamic> _userToMap(User user) {
    return {
      'card': user.conducteur?.carte,
      'name': user.conducteur?.nom,
      'firstName': user.conducteur?.prenom,
      'email': user.conducteur?.email,
      'phone': user.conducteur?.tel,
    };
  }

  static DeviceConnectionState _deviceConnectionState(String state) {
    switch (state) {
      case "CONNECTED":
        return DeviceConnectionState.connected;
      case "DISCONNECTED":
        return DeviceConnectionState.disconnected;
      case "ERROR":
        return DeviceConnectionState.error;
      default:
        return DeviceConnectionState.disconnected;
    }
  }
}
