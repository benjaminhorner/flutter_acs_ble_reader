import Flutter
import UIKit

public class FlutterAcsCardReaderPlugin: NSObject, FlutterPlugin {
    private var channel: FlutterMethodChannel!
    private var smartCardReader: SmartCardReader!
    private var isScanning: Bool = false

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_acs_card_reader", binaryMessenger: registrar.messenger())
        let instance = FlutterAcsCardReaderPlugin()
        instance.channel = channel
        registrar.addMethodCallDelegate(instance, channel: channel)
        registrar.addApplicationDelegate(instance)
        instance.smartCardReader = SmartCardReader(methodChannel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "connectToDevice":
            guard let arguments = call.arguments as? [String: Any],
                  let driver = arguments["driver"] as? [String: Any],
                  let cardTerminalType = arguments["cardTerminalType"] as? Int,
                  let timeoutSeconds = arguments["timeoutSeconds"] as? Int else {
                result(FlutterError(code: "INVALID_DEVICE", message: "Invalid driver, cardTerminalType, or timeoutSeconds parameter", details: nil))
                return
            }
            connectToDevice(result: result, driver: driver, cardTerminalType: cardTerminalType, timeoutSeconds: timeoutSeconds)

        default:
            result(FlutterMethodNotImplemented)
        }
    }

    private func connectToDevice(result: @escaping FlutterResult, driver: [String: Any], cardTerminalType: Int, timeoutSeconds: Int) {
        guard let card = driver["card"] as? String,
              let name = driver["name"] as? String,
              let firstName = driver["firstName"] as? String,
              let email = driver["email"] as? String,
              let phone = driver["phone"] as? String,
              let agencyID = driver["agencyID"] as? String else {
            result(FlutterError(code: "INVALID_DEVICE", message: "Invalid parameters", details: nil))
            return
        }

        let driver = Driver(card: card, name: name, firstName: firstName, email: email, phone: phone, agencyID: agencyID)
        print("DRIVER \(driver) cardTerminalType \(cardTerminalType) timeoutSeconds \(timeoutSeconds)");
        smartCardReader.connectToDevice(driver: driver, cardTerminalType: cardTerminalType, timeoutSeconds: timeoutSeconds)
    }
}
