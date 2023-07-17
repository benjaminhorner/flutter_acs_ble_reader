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
  String _deviceInformation = "";
  bool _isScanning = false;

  @override
  void initState() {
    super.initState();
    _listentToScanResults();
  }

  void _startScan() {
    setState(() {
      _isScanning = true;
    });
    FlutterAcsCardReader.scanSmartCardDevices();
  }

  void _stopScan() {
    FlutterAcsCardReader.stopScanningSmartCardDevices().then((_) {
      setState(() {
        _isScanning = false;
      });
    });
  }

  void _listentToScanResults() {
    FlutterAcsCardReader.listentToScanResults()
        .listen((List<ScanResult> results) {
      for (ScanResult result in results) {
        if (result.device.name.contains('ACR')) {
          setState(() {
            _deviceInformation = result.device.name;
            FlutterAcsCardReader.readSmartCard(result.device);
            _isScanning = false;
            _stopScan();
          });
        }
      }
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
              Text('Found device: $_deviceInformation\n'),
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
