import 'dart:async';

// Import
import 'package:flutter/foundation.dart';
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

  // Stream controllers for each type of event
  static final StreamController<DeviceConnectionState>
      _deviceConnectionStateController =
      StreamController<DeviceConnectionState>.broadcast();
  static final StreamController<BluetoothDevice> _deviceFoundEventController =
      StreamController<BluetoothDevice>.broadcast();
  static final StreamController<bool> _locationIsGrantedController =
      StreamController<bool>.broadcast();

  // Streams to expose for listening to events
  static Stream<DeviceConnectionState> get deviceConnectionStateStream =>
      _deviceConnectionStateController.stream;
  static Stream<BluetoothDevice> get deviceFoundEventStream =>
      _deviceFoundEventController.stream;
  static Stream<bool> get locationIsGrantedStream =>
      _locationIsGrantedController.stream;

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

  /// Start Streams
  ///
  static void startListeningToEvents() {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onDeviceConnectionStatusEvent') {
        final dynamic eventData = call.arguments;
        debugPrint("onDeviceConnectionStatusEvent is $eventData");
        switch (eventData) {
          case "SEARCHING":
            _deviceConnectionStateController
                .add(DeviceConnectionState.searching);
            break;
          case "STOPPED":
            _deviceConnectionStateController.add(DeviceConnectionState.stopped);
            break;
          default:
            _deviceConnectionStateController.add(DeviceConnectionState.error);
        }
      } else if (call.method == 'onDeviceFoundEvent') {
        try {
          final dynamic eventData = call.arguments;
          debugPrint("onDeviceFoundEvent is $eventData");
          final BluetoothDevice device = BluetoothDevice.fromMap(eventData);
          _deviceFoundEventController.add(device);
        } catch (e) {
          rethrow;
        }
      } else if (call.method == 'onLocationPermissionResult') {
        bool granted = call.arguments as bool;
        debugPrint("onLocationPermissionResult is $granted");
        _locationIsGrantedController.add(granted);
      }
    });
  }

  /// Stop Streams
  void stopListeningToDeviceConnectionStatusEvents() {
    _deviceConnectionStateController.close();
  }

  void stopListeningToDeviceFoundEvents() {
    _deviceFoundEventController.close();
  }

  void stopListeningToLocationIsGrantedEvents() {
    _locationIsGrantedController.close();
  }
}
