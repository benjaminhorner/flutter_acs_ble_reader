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
  bool _isScanning = false;

  void _startScan() {
    setState(() {
      _isScanning = true;
      _bleActivity = "Searching…";
    });
    FlutterAcsCardReader.scanSmartCardDevices().then((results) {
      if (results.isNotEmpty) {
        setState(() {
          _bleActivity = "Found device: ${results[0].name}";
        });
      }
    });
  }

  void _stopScan() async {
    FlutterAcsCardReader.stopScanningSmartCardDevices().then((_) {
      setState(() {
        _bleActivity = "Stopped";
        _isScanning = false;
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
