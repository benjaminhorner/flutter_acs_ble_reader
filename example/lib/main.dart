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
  StreamSubscription<DeviceSearchState?>? _deviceSearchStateStream;
  StreamSubscription<DeviceConnectionState?>? _deviceConnectionStateStream;
  StreamSubscription<BluetoothDevice?>? _deviceFoundEventStream;
  String _deviceActivity = "";
  String _deviceConnectionState = "";
  String _deviceName = "";
  String _locationGrantedStatus = "UNKNOWN";
  final String _cardActivity = "";
  bool _isScanning = false;

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
    FlutterAcsCardReader.scanSmartCardDevices();
  }

  void _stopScan() {
    FlutterAcsCardReader.stopScanningSmartCardDevices();
  }

  void _readCard(BluetoothDevice device) async {
    User user = User.fromJson({
      "conducteur": {
        "nom": "HORNER",
        "prenom": "Benjamin",
        "tel": "",
        "email": "b.e.horner@gmail.com",
        "carte": "10000000074810"
      },
      "agence": {"ID": 1, "emails": "ventes@sogestmatic.com"},
      "estConnecte": true
    });
    await FlutterAcsCardReader.readSmartCard(
      device,
      user: user,
    );
  }

  void _registerListeners() {
    FlutterAcsCardReader.startListeningToEvents();

    // Listen to device search status
    _deviceSearchStateStream = FlutterAcsCardReader.deviceSearchStateStream
        .listen((DeviceSearchState state) {
      setState(() {
        _isScanning = state == DeviceSearchState.searching ? true : false;
        _deviceActivity = state.toString();
      });
    });

    // Listen to Device connection status
    _deviceConnectionStateStream = FlutterAcsCardReader
        .deviceConnectionStateStream
        .listen((DeviceConnectionState state) {
      setState(() {
        _deviceConnectionState = state.toString();
      });
    });

    // Listen to Location Status
    FlutterAcsCardReader.bluetoothStatusStream.listen((BluetoothStatus status) {
      setState(() {
        _locationGrantedStatus = status.toString();
      });
    });

    // Listen to Found devices
    FlutterAcsCardReader.deviceFoundEventStream
        .listen((BluetoothDevice device) {
      setState(() {
        _deviceName = device.localName;
      });
      _readCard(device);
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
              Text("Location status: $_locationGrantedStatus"),
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
              Text("Card state: $_cardActivity"),
              const SizedBox(
                height: 8,
              ),
              ElevatedButton(
                onPressed: _isScanning ? _stopScan : _startScan,
                child: Text(_isScanning ? "Stop Scan" : "Start Scan"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
