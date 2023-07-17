import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_acs_card_reader_method_channel.dart';

abstract class FlutterAcsCardReaderPlatform extends PlatformInterface {
  /// Constructs a FlutterAcsCardReaderPlatform.
  FlutterAcsCardReaderPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAcsCardReaderPlatform _instance =
      MethodChannelFlutterAcsCardReader();

  /// The default instance of [FlutterAcsCardReaderPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAcsCardReader].
  static FlutterAcsCardReaderPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAcsCardReaderPlatform] when
  /// they register themselves.
  static set instance(FlutterAcsCardReaderPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }
}
