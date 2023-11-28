import 'dart:async';
import 'dart:convert';
import 'dart:io';

// Import
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_acs_card_reader/enums/card_connection_state.enum.dart';
import 'package:flutter_acs_card_reader/enums/card_terminal_type.enum.dart';
import 'package:flutter_acs_card_reader/enums/data_transfer_state.enum.dart';
import 'package:flutter_acs_card_reader/enums/device_connection_state.enum.dart';
import 'package:flutter_acs_card_reader/models/card_terminal.model.dart';
import 'package:flutter_acs_card_reader/models/data_transfer.model.dart';
import 'enums/device_search_state.enum.dart';
import 'models/user.model.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';

/// Export
export 'enums/card_connection_state.enum.dart';
export 'enums/device_search_state.enum.dart';
export 'package:flutter_blue_plus/flutter_blue_plus.dart';
export 'package:flutter_acs_card_reader/enums/device_connection_state.enum.dart';
export 'models/user.model.dart';
export 'package:flutter_acs_card_reader/models/card_terminal.model.dart';
export 'package:flutter_acs_card_reader/enums/data_transfer_state.enum.dart';
export 'package:flutter_acs_card_reader/models/data_transfer.model.dart';

class FlutterAcsCardReader {
  static Timer? _timeOutTimer;
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
  static final StreamController<CardTerminal> _deviceFoundEventController =
      StreamController<CardTerminal>.broadcast();
  static final StreamController<BluetoothAdapterState>
      _bluetoothStatusController =
      StreamController<BluetoothAdapterState>.broadcast();
  static final StreamController<CardConnectionState>
      _cardConnectionStateEventController =
      StreamController<CardConnectionState>.broadcast();
  static final StreamController<int> _totalReadStepsStateEventController =
      StreamController<int>.broadcast();
  static final StreamController<int> _currentReadStepStateEventController =
      StreamController<int>.broadcast();
  static final StreamController<DataTransferState>
      _dataTransferStateEventController =
      StreamController<DataTransferState>.broadcast();
  static final StreamController<ResponseData> _dataTransferController =
      StreamController<ResponseData>.broadcast();
  static final StreamController<String> _logDataController =
      StreamController<String>.broadcast();

  // Streams to expose for listening to events
  static Stream<DeviceSearchState> get deviceSearchStateStream =>
      _deviceSearchStateController.stream;
  static Stream<DeviceConnectionState> get deviceConnectionStateStream =>
      _deviceConnectionStateController.stream;
  static Stream<CardTerminal> get deviceFoundEventStream =>
      _deviceFoundEventController.stream;
  static Stream<BluetoothAdapterState> get bluetoothStatusStream =>
      _bluetoothStatusController.stream;
  static Stream<CardConnectionState> get cardConnectionStateStream =>
      _cardConnectionStateEventController.stream;
  static Stream<int> get totalReadStepsStateStream =>
      _totalReadStepsStateEventController.stream;
  static Stream<int> get currentReadStepStateStream =>
      _currentReadStepStateEventController.stream;
  static Stream<DataTransferState> get dataTransferStateStream =>
      _dataTransferStateEventController.stream;
  static Stream<ResponseData> get dataTransferStream =>
      _dataTransferController.stream;
  static Stream<String> get logDataStream => _logDataController.stream;

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
      _timeOutTimer?.cancel();
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
          } else if (connectionState == DeviceConnectionState.disconnected) {
            stopScanningSmartCardDevices();
          }
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onDeviceConnectionStatusEvent] $exception");
        }
      } else if (call.method == 'onDeviceFoundEvent') {
        try {
          final dynamic device = call.arguments;
          debugPrint("onDeviceFoundEvent $device");
          CardTerminal cardTerminal = _mapToCardTerminal(device);
          _deviceFoundEventController.add(cardTerminal);
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onDeviceFoundEvent] $exception");
        }
      } else if (call.method == 'onCardConnectionEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onCardConnectionEvent $state");
          CardConnectionState cardConnectionState =
              _mapToCardConnectionState(state);
          _cardConnectionStateEventController.add(cardConnectionState);
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onCardConnectionEvent] $exception");
        }
      } else if (call.method == 'onUpdateTotalReadStepsEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onUpdateTotalReadStepsEvent $state");
          _totalReadStepsStateEventController.add(state);
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onUpdateTotalReadStepsEvent] $exception");
        }
      } else if (call.method == 'onUpdateCurrentReadStepEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onUpdateCurrentReadStepEvent $state");
          _currentReadStepStateEventController.add(state);
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onUpdateCurrentReadStepEvent] $exception");
        }
      } else if (call.method == 'onUpdateDataTransferStateEvent') {
        try {
          final dynamic state = call.arguments;
          debugPrint("onUpdateDataTransferStateEvent $state");
          _dataTransferStateEventController.add(_mapToDataTransferState(state));
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onUpdateDataTransferStateEvent] $exception");
        }
      } else if (call.method == 'onReceiveDataEvent') {
        try {
          final dynamic data = call.arguments;
          debugPrint("onReceiveDataEvent $data");
          _dataTransferController.add(_jsonStringToResponseData(data));
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onReceiveDataEvent] $exception");
        }
      } else if (call.method == 'onReceiveLogDataEvent') {
        try {
          final dynamic data = call.arguments;
          debugPrint("onReceiveLogDataEvent $data");
          _logDataController.add(data);
        } catch (exception, stackTrace) {
          debugPrintStack(stackTrace: stackTrace);
          debugPrint("[onReceiveLogDataEvent] $exception");
        }
      }
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
    if (await FlutterBluePlus.isSupported == false) {
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
        .where(
          (s) => s == BluetoothAdapterState.on,
        )
        .first
        .then((value) => null);

    /// Set listener for Scan results
    ///
    _setScanResultsListener(
      user: user,
      timeoutSeconds: timeoutSeconds,
    );

    /// Scan for BLE devices
    /// Returns the Card terminal type
    ///
    await FlutterBluePlus.startScan(
      timeout: Duration(
        seconds: timeoutSeconds,
      ),
    );
  }

  static _setScanResultsListener({
    required User user,
    required int timeoutSeconds,
  }) async {
    try {
      scanResultsSubscription =
          FlutterBluePlus.scanResults.listen((results) async {
        for (ScanResult result in results) {
          BluetoothDevice device = result.device;
          CardTerminalType? type = _cardTerminalType(device);
          if (type is CardTerminalType) {
            _connectToCardTerminal(
              user: user,
              cardTerminalDeviceType: type,
              timeoutSeconds: timeoutSeconds,
            );
            await stopScanningSmartCardDevices();
          }
        }
      });
      _timeOutTimer = Timer(
        Duration(seconds: timeoutSeconds),
        () async => await stopScanningSmartCardDevices(),
      );
    } catch (exception, stackTrace) {
      debugPrintStack(stackTrace: stackTrace);
      debugPrint("[FlutterBluePlus.scanResults] $exception");
      await stopScanningSmartCardDevices();
    }
  }

  static Future<void> _connectToCardTerminal({
    required User user,
    required CardTerminalType cardTerminalDeviceType,
    required int timeoutSeconds,
  }) async {
    debugPrint(
        "Start searching for Card Terminals of type $cardTerminalDeviceType");
    try {
      Map<String, dynamic> mappedUser = _userToMap(user);
      await _channel.invokeMethod(
        'connectToDevice',
        {
          'driver': mappedUser,
          'cardTerminalType': cardTerminalDeviceType.index,
          'timeoutSeconds': timeoutSeconds
        },
      );
    } catch (e) {
      throw Exception('Error reading smart card: $e');
    }
  }

  static CardTerminal _mapToCardTerminal(dynamic map) {
    return CardTerminal(
      name: map['name'],
      isCardPresent: map['isCardPresent'],
    );
  }

  static CardTerminalType? _cardTerminalType(BluetoothDevice device) {
    String name = device.platformName;

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

  static CardConnectionState _mapToCardConnectionState(String state) {
    switch (state) {
      case "BONDING":
        return CardConnectionState.bonding;
      case "CONNECTED":
        return CardConnectionState.connected;
      case "DISCONNECTED":
        return CardConnectionState.disconnected;
      default:
        return CardConnectionState.disconnected;
    }
  }

  static DataTransferState _mapToDataTransferState(String state) {
    switch (state) {
      case "PENDING":
        return DataTransferState.pending;
      case "TRANSFERING":
        return DataTransferState.transfering;
      case "SUCCESS":
        return DataTransferState.success;
      default:
        return DataTransferState.error;
    }
  }

  static Map<String, dynamic> _userToMap(User user) {
    return {
      'card': user.conducteur?.carte,
      'name': user.conducteur?.nom,
      'firstName': user.conducteur?.prenom,
      'email': user.conducteur?.email,
      'phone': user.conducteur?.tel,
      'agencyID': user.agence?.iD.toString(),
    };
  }

  static ResponseData _jsonStringToResponseData(String response) {
    try {
      Map<String, dynamic> json = jsonDecode(response);
      return ResponseData.fromJson(json);
    } catch (exception) {
      rethrow;
    }
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
