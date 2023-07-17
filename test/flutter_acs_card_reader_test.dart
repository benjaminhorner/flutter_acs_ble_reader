import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_acs_card_reader/flutter_acs_card_reader_platform_interface.dart';
import 'package:flutter_acs_card_reader/flutter_acs_card_reader_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterAcsCardReaderPlatform
    with MockPlatformInterfaceMixin
    implements FlutterAcsCardReaderPlatform {}

void main() {
  final FlutterAcsCardReaderPlatform initialPlatform =
      FlutterAcsCardReaderPlatform.instance;

  test('$MethodChannelFlutterAcsCardReader is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterAcsCardReader>());
  });
}
