import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_acs_card_reader/flutter_acs_card_reader.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final User user = User.fromJson(
    {
      "conducteur": {
        "nom": "HORNER",
        "prenom": "Benjamin",
        "tel": "",
        "email": "b.e.horner@gmail.com",
        "carte": "10000000074810"
      },
      "agence": {"ID": 1, "emails": "ventes@sogestmatic.com"},
      "estConnecte": true
    },
  );
  StreamSubscription<DeviceSearchState?>? _deviceSearchStateStream;
  StreamSubscription<DeviceConnectionState?>? _deviceConnectionStateStream;
  StreamSubscription<BluetoothDevice?>? _deviceFoundEventStream;
  BluetoothAdapterState _bluetoothState = BluetoothAdapterState.unknown;
  DeviceSearchState _deviceActivity = DeviceSearchState.stopped;
  DeviceConnectionState _deviceConnectionState = DeviceConnectionState.pending;
  String _deviceName = "";
  bool _isScanning = false;
  CardConnectionState _cardConnectionState = CardConnectionState.disconnected;
  int _totalReadSteps = 0;
  int _currentReadStep = 0;
  DataTransferState _dataTransferState = DataTransferState.pending;
  String _data = "";

  @override
  void initState() {
    super.initState();
    _registerListeners();
  }

  @override
  void dispose() {
    super.dispose();
    _deviceSearchStateStream?.cancel();
    _deviceFoundEventStream?.cancel();
    _deviceConnectionStateStream?.cancel();
  }

  void _startScan() {
    FlutterAcsCardReader.scanSmartCardDevices(user);
  }

  void _stopScan() {
    FlutterAcsCardReader.stopScanningSmartCardDevices();
  }

  void _registerListeners() {
    FlutterAcsCardReader.startListeningToEvents();

    // Listen to device search status
    _deviceSearchStateStream = FlutterAcsCardReader.deviceSearchStateStream
        .listen((DeviceSearchState state) {
      setState(() {
        _isScanning = state == DeviceSearchState.searching ? true : false;
        _deviceActivity = state;
      });
    });

    // Listen to Device connection status
    _deviceConnectionStateStream = FlutterAcsCardReader
        .deviceConnectionStateStream
        .listen((DeviceConnectionState state) {
      setState(() {
        _deviceConnectionState = state;
      });
    });

    // Listen to Location Status
    FlutterAcsCardReader.bluetoothStatusStream
        .listen((BluetoothAdapterState state) {
      setState(() {
        _bluetoothState = state;
      });
    });

    // Listen to Found devices
    FlutterAcsCardReader.deviceFoundEventStream.listen((CardTerminal terminal) {
      setState(() {
        _deviceName = terminal.name ?? "NO NAME";
      });
    });

    // Listen to Card connection State
    FlutterAcsCardReader.cardConnectionStateStream
        .listen((CardConnectionState state) {
      setState(() {
        _cardConnectionState = state;
      });
    });

    // Listen to Read Steps
    FlutterAcsCardReader.totalReadStepsStateStream.listen((int state) {
      setState(() {
        _totalReadSteps = state;
      });
    });

    // Listen to Read Steps
    FlutterAcsCardReader.currentReadStepStateStream.listen((int state) {
      setState(() {
        _currentReadStep = state;
      });
    });

    // Listen to Data Transfer State
    FlutterAcsCardReader.dataTransferStateStream
        .listen((DataTransferState state) {
      setState(() {
        _dataTransferState = state;
      });
    });

    // Listen to Data Transfer
    FlutterAcsCardReader.dataTransferStream.listen((String data) {
      setState(() {
        _data = data;
        debugPrint("Received Data: $data");
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text("Location status: $_bluetoothState"),
              const SizedBox(
                height: 8,
              ),
              Text("Device search state: $_deviceActivity"),
              const SizedBox(
                height: 8,
              ),
              Text("Device connection state: $_deviceConnectionState"),
              const SizedBox(
                height: 8,
              ),
              Text("Found device: $_deviceName"),
              const SizedBox(
                height: 8,
              ),
              Text("Card state: $_cardConnectionState"),
              const SizedBox(
                height: 8,
              ),
              Text("Data Transfer state: $_dataTransferState"),
              const SizedBox(
                height: 8,
              ),
              Text(
                  "Reading APDU Progress: ${_totalReadSteps > 0 ? ((_currentReadStep / _totalReadSteps) * 100).toInt() : 0}%"),
              const SizedBox(
                height: 8,
              ),
              Text("Received data of length: ${_data.length}"),
              const SizedBox(
                height: 8,
              ),
              Text(
                  "${_data.isNotEmpty ? _data.substring(0, 2) : 0} … ${_data.isNotEmpty ? _data.substring(_data.length - 2, _data.length) : 0}"),
              const SizedBox(
                height: 8,
              ),
              ElevatedButton(
                onPressed:
                    _deviceConnectionState == DeviceConnectionState.connected
                        ? null
                        : _isScanning
                            ? _stopScan
                            : _startScan,
                child: Text(_isScanning ? "Stop Scan" : "Start Scan"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
