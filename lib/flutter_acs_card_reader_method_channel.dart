import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_acs_card_reader_platform_interface.dart';

/// An implementation of [FlutterAcsCardReaderPlatform] that uses method channels.
class MethodChannelFlutterAcsCardReader extends FlutterAcsCardReaderPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_acs_card_reader');
}
