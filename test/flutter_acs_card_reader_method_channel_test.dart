import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
//import 'package:flutter_acs_card_reader/flutter_acs_card_reader_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  //MethodChannelFlutterAcsCardReader platform =
  //    MethodChannelFlutterAcsCardReader();
  const MethodChannel channel = MethodChannel('flutter_acs_card_reader');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });
}
