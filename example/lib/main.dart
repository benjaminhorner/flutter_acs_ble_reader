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
  String _bleActivity = "";
  final bool _isScanning = false;

  @override
  void initState() {
    super.initState();
    _registerListeners();
  }

  void _startScan() {
    FlutterAcsCardReader.scanSmartCardDevices().then((results) {
      if (results.isNotEmpty) {
        BluetoothDevice device = results[0];
        _readCard(device);
        setState(() {
          _bleActivity = "Found device: ${device.name}";
        });
      }
    });
  }

  void _stopScan() async {
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
    FlutterAcsCardReader.registerDeviceConnectionStateListener(
        (DeviceConnectionState state) {
      setState(() {
        _bleActivity = state.toString();
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
              Text(_bleActivity),
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
