import 'dart:async';
import 'dart:io';

// Import
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_acs_card_reader/enums/card_terminal_type.enum.dart';
import 'package:flutter_acs_card_reader/enums/device_connection_state.enum.dart';
import 'enums/device_search_state.enum.dart';
import 'models/user.model.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'enums/device_search_state.enum.dart';
export 'package:flutter_blue_plus/flutter_blue_plus.dart';
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
  static final StreamController<BluetoothAdapterState>
      _bluetoothStatusController =
      StreamController<BluetoothAdapterState>.broadcast();

  // Streams to expose for listening to events
  static Stream<DeviceSearchState> get deviceSearchStateStream =>
      _deviceSearchStateController.stream;
  static Stream<DeviceConnectionState> get deviceConnectionStateStream =>
      _deviceConnectionStateController.stream;
  static Stream<BluetoothDevice> get deviceFoundEventStream =>
      _deviceFoundEventController.stream;
  static Stream<BluetoothAdapterState> get bluetoothStatusStream =>
      _bluetoothStatusController.stream;

  /// Public
  ///
  static Future<void> scanSmartCardDevices(User user,
      {int timeoutSeconds = 10}) async {
    try {
      _deviceSearchStateController.add(DeviceSearchState.searching);
      await _scanForDevices(timeoutSeconds, user: user);
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

  /// Start Streams
  ///
  static void startListeningToEvents() {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onDeviceConnectionStatusEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onDeviceConnectionStatusEvent is $state");
          DeviceConnectionState connectionState = _deviceConnectionState(state);
          _deviceConnectionStateController.add(connectionState);
          if (connectionState == DeviceConnectionState.error) {
            stopScanningSmartCardDevices();
          }
        } catch (e) {
          rethrow;
        }
      }
      // else if (call.method == 'onDeviceConnectionStatusEvent') {
      //   try {
      //     final dynamic state = call.arguments;
      //     debugPrint("onReaderDetectionStatusEvent is $state");

      //   } catch (e) {
      //     rethrow;
      //   }
      // }
    });
  }

  /// Stop Streams
  static Future<void> stopListeningToLocationIsGrantedEvents() async {
    await _bluetoothStatusController.close();
  }

  static Future<void> _stopListeningToScanResults() async {
    await FlutterBluePlus.stopScan();
    await scanResultsSubscription?.cancel();
  }

  /// Private
  ///
  static Future<void> _scanForDevices(int timeoutSeconds,
      {required User user}) async {
    /// check adapter availability
    if (await FlutterBluePlus.isAvailable == false) {
      debugPrint("Bluetooth not supported by this device");
      _bluetoothStatusController.add(BluetoothAdapterState.unavailable);
      return;
    }

    /// turn on bluetooth if we can (Android only)
    /// for iOS, the user controls bluetooth enable/disable
    if (Platform.isAndroid) {
      await FlutterBluePlus.turnOn();
    }

    /// wait for bluetooth to be on & start searching for devices
    /// note: for iOS the initial state is typically BluetoothAdapterState.unknown
    /// note: if you have permissions issues you will get stuck at BluetoothAdapterState.unauthorized
    await FlutterBluePlus.adapterState
        .map((state) {
          debugPrint(state.toString());
          return state;
        })
        .where((s) => s == BluetoothAdapterState.on)
        .first;

    /// Set listener for Scan results
    ///
    _setScanResultsListener(user: user);

    /// Scan for BLE devices
    /// Returns the Card terminal type
    ///
    await FlutterBluePlus.startScan();
  }

  static _setScanResultsListener({required User user}) {
    try {
      scanResultsSubscription =
          FlutterBluePlus.scanResults.listen((results) async {
        for (ScanResult result in results) {
          BluetoothDevice device = result.device;
          CardTerminalType? type = _cardTerminalType(device);
          if (type is CardTerminalType) {
            _deviceFoundEventController.add(device);
            _connectToCardTerminal(
              user: user,
              cardTerminalDeviceType: type,
            );
            await _stopListeningToScanResults();
          }
        }
      });
    } catch (exception, stackTrace) {
      debugPrintStack(stackTrace: stackTrace);
      debugPrint("[FlutterBluePlus.scanResults] $exception");
    }
  }

  static Future<void> _connectToCardTerminal({
    required User user,
    required CardTerminalType cardTerminalDeviceType,
  }) async {
    debugPrint(
        "Start searching for Card Terminals of type $cardTerminalDeviceType");
    try {
      Map<String, dynamic> mappedUser = _userToMap(user);
      await _channel.invokeMethod(
        'connectToDevice',
        {'driver': mappedUser},
      );
    } catch (e) {
      throw Exception('Error reading smart card: $e');
    }
  }

  static CardTerminalType? _cardTerminalType(BluetoothDevice device) {
    String name = device.localName;

    debugPrint("Found device with name $name");

    if (name.contains("ACR")) {
      if (name.contains("ACR3901")) {
        return CardTerminalType.acr3901;
      } else if (name.contains("ACR1255")) {
        return CardTerminalType.acr1255;
      } else if (name.contains("AMR220")) {
        return CardTerminalType.amr220;
      } else {
        return CardTerminalType.acr1255v2;
      }
    }

    return null;
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
