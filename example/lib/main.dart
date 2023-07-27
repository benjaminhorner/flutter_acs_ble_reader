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
  String _deviceActivity = "";
  final String _deviceName = "";
  final String _cardActivity = "";
  bool _isScanning = false;

  @override
  void initState() {
    super.initState();
    _registerListeners();
  }

  void _startScan() {
    FlutterAcsCardReader.scanSmartCardDevices();
  }

  void _stopScan() {
    FlutterAcsCardReader.stopScanningSmartCardDevices();
  }

  void _readCard(BluetoothDevice device) {
    FlutterAcsCardReader.readSmartCard(device).then(
      (value) {
        print(value);
      },
    );
  }

  void _registerListeners() {
    FlutterAcsCardReader.registerDeviceConnectionStatusEventListener(
        (DeviceConnectionState state) {
      setState(() {
        _isScanning = state == DeviceConnectionState.searching ? true : false;
        _deviceActivity = state.toString();
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
              Text("Device state: $_deviceActivity"),
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
