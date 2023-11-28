import Foundation
import SmartCardIO
import ACSSmartCardIO
import Flutter

private let TAG = "SmartCardReader"
private let UNABLE_TO_TRANSMIT_APDU_EXCEPTION = "Unable to transmit APDU"
private let UNABLE_TO_CREATE_HASH_EXCEPTION = "Unable to Create Hash"
private let UNABLE_TO_PERFORM_SELECTION = "Unable to select from card"
private let UNABLE_TO_CONNECT_TO_CARD = "Unable to connect to card"
private let HEX_DOES_NOT_CONTAIN_ENOUGH_BYTES = "Hex string does not contain enough bytes."
private let DATA_TRANSFER_STATE_PENDING = "PENDING"
private let DATA_TRANSFER_STATE_TRANSFERING = "TRANSFERING"
private let DATA_TRANSFER_STATE_SUCCESS = "SUCCESS"
private let DATA_TRANSFER_STATE_ERROR = "ERROR"
private let CARD_STATE_BONDING = "BONDING"
private let CARD_STATE_CONNECTED = "CONNECTED"
private let CARD_STATE_DISCONNECTED = "DISCONNECTED"
private let DEVICE_CONNECTION_STATE_ERROR = "ERROR"
private let UNABLE_TO_SIGN_APDU_EXCEPTION = "Unable to Sign APDU"
private let apduCommandListGenerator = ApduCommandListGenerator()
private let hexToBytesHelper = HexHelper()
private let apduResponseHelper = APDUResponseHelper()
private let countryCodeHelper = CountryCodeHelper()
private let deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private let deviceNotifier = DeviceNotifier()
private let dataTransferStateNotifier = DataTransferStateNotifier()
private let currentReadStepStatusNotifier = CurrentReadStepStatusNotifier()
private let dataTransferNotifier = DataTransferNotifier()
private let cardConnectionStateNotifier = CardConnectionStateNotifier()
private let totalReadStepsStatusNotifier = TotalReadStepsStatusNotifier()

class SmartCardReader: BluetoothTerminalManagerDelegate {
    private var methodChannel: FlutterMethodChannel
    private var driver: Driver?
    private var cardTerminalType: Int = 0
    private var mManager: BluetoothTerminalManager?
    private var mFactory: TerminalFactory?
    private var timeoutSeconds: Int = 10
    private var cardStructureVersion: CardGen?
    private var signatureVersion: CardGen?
    private var noOfVarModel: NoOfVarModel
    private var totalUploadSteps: Int = 0
    private var uploadSteps: Int = 0
    private var c1BFileData: String = ""
    private var treatedAPDU: ApduData
    private var apduList: [ApduCommand] = []
    private let maxSignatureLength: Int = 132
    private var signatureLength: Int = 64
    private var countryCode: String = ""

    init(methodChannel: FlutterMethodChannel) {
         self.methodChannel = methodChannel
         self.noOfVarModel = NoOfVarModel()
         self.treatedAPDU = ApduData()
    }

    func connectToDevice(
         driver: Driver,
         cardTerminalType: Int,
         timeoutSeconds: Int
    ) {
        self.driver = driver
        self.cardTerminalType = cardTerminalType
        self.timeoutSeconds = timeoutSeconds

        mManager = BluetoothSmartCard.shared.manager
        guard let mManager = mManager else {
            print("\(TAG): connectToDevice mManager cannot be null")
            deviceConnectionStatusNotifier.updateState(state: DEVICE_CONNECTION_STATE_ERROR, channel: methodChannel)
            return
        }
        mManager.delegate = self

        mFactory = BluetoothSmartCard.shared.factory
        guard mFactory != nil else {
            print("\(TAG): connectToDevice mFactory cannot be null")
            deviceConnectionStatusNotifier.updateState(state: DEVICE_CONNECTION_STATE_ERROR, channel: methodChannel)
            return
        }
        return
    }
    
    private func toCardTerminalType() -> BluetoothTerminalManager.TerminalType {
        switch (cardTerminalType) {
        case 0:
            return BluetoothTerminalManager.TerminalType.acr3901us1
        case 1:
            return BluetoothTerminalManager.TerminalType.acr1255uj1
        case 2:
            return BluetoothTerminalManager.TerminalType.amr220c
        case 3:
            return BluetoothTerminalManager.TerminalType.acr1255uj1v2
        default:
            return BluetoothTerminalManager.TerminalType.acr1255uj1
        }
    }

    private func startScan(timeoutSeconds: Int) throws {
        print("\(TAG): Start scan")
        let terminalType: BluetoothTerminalManager.TerminalType = toCardTerminalType()
        print("\(TAG): Start scanning for \(terminalType)")
        if (mManager == nil) {
            throw ACSError.unableToConnectToCard(description: UNABLE_TO_CONNECT_TO_CARD)
        } else {
            mManager?.startScan(terminalType: terminalType)
            DispatchQueue.main.asyncAfter(deadline: .now() + Double(timeoutSeconds)) {
                self.mManager?.stopScan()
            }
        }
     }
    
    
    // MARK: BluetoothTerminalManagerDelegate
    //
    func bluetoothTerminalManagerDidUpdateState(_ manager: ACSSmartCardIO.BluetoothTerminalManager) {
        var message = ""

        switch manager.centralManager.state {

        case .unknown, .resetting:
            message = "The update is being started. Please wait until Bluetooth is ready."

        case .unsupported:
            message = "This device does not support Bluetooth low energy."

        case .unauthorized:
            message = "This app is not authorized to use Bluetooth low energy."

        case .poweredOff:
            message = "You must turn on Bluetooth in Settings in order to use the reader."

        default:
            break
        }
        
        print("\(TAG) - bluetoothTerminalManagerDidUpdateState message \(message)")
        
        do {
            try startScan(timeoutSeconds: timeoutSeconds)
        } catch {
            print("\(TAG) - bluetoothTerminalManagerDidUpdateState error \(error.localizedDescription)")
        }
    }
    
    func bluetoothTerminalManager(_ manager: ACSSmartCardIO.BluetoothTerminalManager, didDiscover terminal: SmartCardIO.CardTerminal) {
        print("\(TAG) - bluetoothTerminalManager: CardTerminal discovered \(terminal.name)")
        if (terminal.name.contains("ACR")) {
            print("\(TAG) - bluetoothTerminalManager: Discovered card containing \(terminal.name)")
            mManager?.stopScan()
            print("\(TAG) - bluetoothTerminalManager: Stopped scanning")
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                do {
                    try deviceNotifier.updateState(terminal: terminal, channel: self.methodChannel)
                    print("\(TAG) - bluetoothTerminalManager: Update device name state")
                    deviceConnectionStatusNotifier.updateState(state: CARD_STATE_CONNECTED, channel: self.methodChannel)
                    print("\(TAG) - bluetoothTerminalManager: Update device connection state")
                    print("\(TAG) - bluetoothTerminalManager: connecting to Card")
                    self.connectToCard(terminal: terminal, methodChannel: self.methodChannel)
                    print("\(TAG) - bluetoothTerminalManager: connected to Card")
                } catch {
                    self.handleError(error: error.localizedDescription, methodChannel: self.methodChannel)
                }
            }
        }
    }
    
    private func handleError(error: String, methodChannel: FlutterMethodChannel) {
        do {
            print("\(TAG) iOS handleError \(error)")
            try disconnectCard(methodChannel: methodChannel)
            dataTransferStateNotifier.updateState(state: DATA_TRANSFER_STATE_ERROR, channel: methodChannel)
            currentReadStepStatusNotifier.updateState(step: uploadSteps, channel: methodChannel)
        } catch {
            dataTransferStateNotifier.updateState(state: DATA_TRANSFER_STATE_ERROR, channel: methodChannel)
            currentReadStepStatusNotifier.updateState(step: uploadSteps, channel: methodChannel)
        }
    }
    
    private func getCardVersion(cardChannel: CardChannel, testGen1: Bool = false) throws {
        do {
            apduList = testGen1 ? apduCommandListGenerator.cardVersionCommandList() : apduCommandListGenerator.cardVersionGen2CommandList()
            try select(cardChannel: cardChannel, getCardVersion: true)
        }
        catch {
            if (error.localizedDescription == UNABLE_TO_TRANSMIT_APDU_EXCEPTION) {
                try getCardVersion(cardChannel: cardChannel, testGen1: true)
            }
            else {
                throw error
            }
        }
    }
    
    private func select(cardChannel: CardChannel, getCardVersion: Bool = false) throws {
        do {
            for (_, apdu) in apduList.enumerated() {
                
                print("\(TAG) select => Selecting APDU \(apdu.name) with command \(apdu.selectCommand)")

                treatedAPDU.name = apdu.name

                let commandAPDU = try CommandAPDU(apdu: hexToBytesHelper.hexStringToByteArray(hexString: apdu.selectCommand))
                let response: ResponseAPDU = try cardChannel.transmit(apdu: commandAPDU)
                let sw1: UInt8? = response.sw1
                let sw2: UInt8? = response.sw2

                if (sw1 == nil && sw2 == nil) {
                    throw ACSError.unableToPerformSelection(description: UNABLE_TO_PERFORM_SELECTION)
                } else {
                    try handleSelectAPDUResponse(
                        status: apduResponseHelper.selectResponseIntToAPDUReadResponse(response: Int(sw1!)),
                        apdu: apdu,
                        cardChannel: cardChannel,
                        getCardVersion: getCardVersion
                    )
                }
            }
        }
        catch {
            throw error
        }
    }
    
    private func handleSelectAPDUResponse(status: APDUSelectResponseEnum, apdu: ApduCommand, cardChannel: CardChannel, getCardVersion: Bool) throws {
        do {
            if (status != APDUSelectResponseEnum.success) {
                throw ACSError.unableToTransmitApduException(description: UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
            }
            else if (apdu.isEF && apdu.needsHash) {
                try performHashCommand(cardChannel: cardChannel)
                print("\(TAG) handleSelectAPDUResponse \(apdu.name) READ 1")
                try read(
                    cardChannel: cardChannel,
                    apdu: apdu,
                    methodChannel: methodChannel,
                    getCardVersion: getCardVersion
                )
            } else if (apdu.isEF) {
                print("\(TAG) handleSelectAPDUResponse \(apdu.name) READ 2")
                try read(
                    cardChannel: cardChannel,
                    apdu: apdu,
                    methodChannel: methodChannel,
                    getCardVersion: getCardVersion
                )
            } else if (apdu.name.contains("DF")) {
                signatureVersion = apdu.name.contains("G2") ? CardGen.GEN2 : CardGen.GEN1
            }
        }
        catch {
            throw error
        }
    }
    
    private func calculateOffset(apdu: ApduCommand) -> String {
        var hexString: String = ""
        if (apdu.isCertificat) {
            hexString = treatedAPDU.offset > 0 ? String(203 + treatedAPDU.offset, radix: 16) : "00"
        } else {
            hexString = String(treatedAPDU.offset * 255, radix: 16)
        }
        return hexToBytesHelper.padHex(hexString: hexString, desiredLength: 4)
    }

    private func calculateExpectedLength(apdu: ApduCommand) -> String {
        if (apdu.isCertificat) {
            let bytes: Int = treatedAPDU.offset > 0 ? 1 : 204
            return hexToBytesHelper.byteLength(apdu: nil, length: bytes)
        } else {
            let bytes: Int = apdu.remainingBytes > 0 ? apdu.remainingBytes : apdu.lengthMin
            if (treatedAPDU.offset >= apdu.maxReadLoops || apdu.maxReadLoops == 0){
                return hexToBytesHelper.byteLength(apdu: nil, length: bytes)
            } else {
                return "FF"
            }
        }
    }
    
    private func buildreadCommand(apdu: ApduCommand) -> String {
        let readCommand = "00 B0 \(calculateOffset(apdu: apdu)) \(calculateExpectedLength(apdu: apdu))"
        print("\(TAG) read buildreadCommand() => Reading APDU \(apdu.name), command: \(readCommand)")
        return readCommand
    }
    
    private func read(cardChannel: CardChannel, apdu: ApduCommand, methodChannel: FlutterMethodChannel, getCardVersion: Bool = false) throws {
            do {
                let commandAPDU = try CommandAPDU(
                    apdu: hexToBytesHelper.hexStringToByteArray(hexString: buildreadCommand(apdu: apdu))
                )
                let response: ResponseAPDU = try cardChannel.transmit(apdu: commandAPDU)
                let sw1: UInt8? = response.sw1
                let sw2: UInt8? = response.sw2

                if (sw1 == nil && sw2 == nil) {
                    print("\(TAG) read => Unable to read card because sw1 and sw2 are NULL")
                    throw ACSError.unableToTransmitApduException(description: UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                } else {
                    try handleReadAPDUResponse(
                        response: response,
                        status: apduResponseHelper.readResponseIntToAPDUReadResponse(response: Int(sw1!)),
                        apdu: apdu,
                        cardChannel: cardChannel,
                        methodChannel: methodChannel,
                        getCardVersion: getCardVersion
                    )
                }
            } catch {
                throw error
            }
    }
    
    private func setCardGenerationAndVersion(generationHex: String, versionHex: String) {
        if (generationHex == "00") {
            cardStructureVersion = CardGen.GEN1
        } else if (generationHex == "01" && versionHex == "00") {
            cardStructureVersion = CardGen.GEN2
        } else {
            cardStructureVersion = CardGen.GEN2V2
        }
    }
    
    private func setCardStructureVersionAndNoOfVariables(hexString: String) throws {
        let hexValues = hexString.components(separatedBy: " ")
        
        if (hexValues.count >= 17) { // Gen 2 Case
            setCardGenerationAndVersion(generationHex: hexValues[1], versionHex: hexValues[2])

            noOfVarModel.noOfEventsPerType = Int(hexValues[3], radix: 16) ?? 0
            noOfVarModel.noOfFaultsPerType = Int(hexValues[4], radix: 16) ?? 0
            noOfVarModel.cardActivityLengthRange = Int(hexValues[5] + hexValues[6], radix: 16) ?? 0
            noOfVarModel.noOfCardVehicleRecords = Int(hexValues[7] + hexValues[8], radix: 16) ?? 0
            noOfVarModel.noOfCardPlaceRecords = Int(hexValues[9] + hexValues[10], radix: 16) ?? 0
            noOfVarModel.noOfGNSSRecords = Int(hexValues[11] + hexValues[12], radix: 16) ?? 0
            noOfVarModel.noOfSpecificConditionsRecords = Int(hexValues[13] + hexValues[14], radix: 16) ?? 0
            noOfVarModel.noOfCardVehicleUnitRecords = Int(hexValues[15] + hexValues[16], radix: 16) ?? 0

            print("\(TAG) Card Structure Hex is: \(hexString)")
            print("\(TAG) Card Structure Card Generation is: \(hexValues[1])")
            print("\(TAG) Card Structure Card version number is: \(hexValues[2])")
            print("\(TAG) Card Structure noOfEventsPerType is: \(noOfVarModel.noOfEventsPerType)")
            print("\(TAG) Card Structure noOfFaultsPerType is: \(noOfVarModel.noOfFaultsPerType)")
            print("\(TAG) Card Structure cardActivityLengthRange is: \(noOfVarModel.cardActivityLengthRange)")
            print("\(TAG) Card Structure noOfCardVehicleRecords is: \(noOfVarModel.noOfCardVehicleRecords)")
            print("\(TAG) Card Structure noOfCardPlaceRecords is: \(noOfVarModel.noOfCardPlaceRecords)")
            print("\(TAG) Card Structure noOfGNSSRecords is: \(noOfVarModel.noOfGNSSRecords)")
            print("\(TAG) Card Structure noOfCardVehicleUnitRecords is: \(noOfVarModel.noOfCardVehicleUnitRecords)")
            print("\(TAG) Card Structure noOfSpecificConditionsRecords is: \(noOfVarModel.noOfSpecificConditionsRecords)")

        } else if (hexValues.count == 10) { // Gen 1 Case
            setCardGenerationAndVersion(generationHex: hexValues[1], versionHex: hexValues[2])

            noOfVarModel.noOfEventsPerType = Int(hexValues[3], radix: 16) ?? 0
            noOfVarModel.noOfFaultsPerType = Int(hexValues[4], radix: 16) ?? 0
            noOfVarModel.cardActivityLengthRange = Int(hexValues[5] + hexValues[6], radix: 16) ?? 0
            noOfVarModel.noOfCardVehicleRecords = Int(hexValues[7] + hexValues[8], radix: 16) ?? 0
            noOfVarModel.noOfCardPlaceRecords = Int(hexValues[9], radix: 16) ?? 0
            
            print("\(TAG) Card Structure Hex is: \(hexString)")
            print("\(TAG) Card Structure Card Generation is: \(hexValues[1])")
            print("\(TAG) Card Structure Card version number is: \(hexValues[2])")
            print("\(TAG) Card Structure noOfEventsPerType is: \(noOfVarModel.noOfEventsPerType)")
            print("\(TAG) Card Structure noOfFaultsPerType is: \(noOfVarModel.noOfFaultsPerType)")
            print("\(TAG) Card Structure cardActivityLengthRange is: \(noOfVarModel.cardActivityLengthRange)")
            print("\(TAG) Card Structure noOfCardVehicleRecords is: \(noOfVarModel.noOfCardVehicleRecords)")
            print("\(TAG) Card Structure noOfCardPlaceRecords is: \(noOfVarModel.noOfCardPlaceRecords)")
            

        } else if (hexValues.count >= 3) { // Card Generation & Version
            print("\(TAG) Card Structure Hex is: \(hexString)")
            print("\(TAG) Card Structure Card Generation is: \(hexValues[1])")
            print("\(TAG) Card Structure Card version number is: \(hexValues[2])")

            setCardGenerationAndVersion(generationHex: hexValues[1], versionHex: hexValues[2])
        }
        else {
            print("\(TAG) Hew does not contain enough bytes: \(hexString)")
            throw ACSError.hexDoesNotContainEnoughBytes(description: HEX_DOES_NOT_CONTAIN_ENOUGH_BYTES)
        }
    }
    
    private func handleCertificateResponse(responseHex: String, status: APDUReadResponseEnum, apdu: ApduCommand, cardChannel: CardChannel, methodChannel: FlutterMethodChannel) throws {
        print("\(TAG) handleCertificateResponse => \(apdu.name) status \(status)")

        do {
            try buildC1BFile(
                hexString: responseHex,
                apdu: apdu,
                cardChannel: cardChannel,
                methodChannel: methodChannel,
                shouldWriteDataToFile: status != APDUReadResponseEnum.success
            )
            if (
                status == APDUReadResponseEnum.success) {
                    print("\(TAG) handleCertificateResponse => \(apdu.name) READ 3")
                    treatedAPDU.offset += 1
                    try self.read(
                        cardChannel: cardChannel,
                        apdu: apdu,
                        methodChannel: methodChannel
                    )
            }
        } catch {
            throw error
        }
    }
    
    private func handleEFResponse(responseHex: String, apdu: ApduCommand, cardChannel: CardChannel, methodChannel: FlutterMethodChannel) throws {
        do {
            let totalBytesLength: Int = hexToBytesHelper.cleanupHexString(hexString: treatedAPDU.data).count/2 + hexToBytesHelper.cleanupHexString(hexString: responseHex).count/2

            print("\(TAG) handleEFResponse => \(apdu.name) totalBytesLength == apdu.calculatedLength ? \(totalBytesLength == apdu.calculatedLength) totalBytesLength \(totalBytesLength) apdu.calculatedLength \(apdu.calculatedLength)")

            try buildC1BFile(
                hexString: responseHex,
                apdu: apdu,
                cardChannel: cardChannel,
                methodChannel: methodChannel,
                shouldWriteDataToFile: totalBytesLength == apdu.calculatedLength
                )

            if (treatedAPDU.offset <= apdu.maxReadLoops && treatedAPDU.name.count > 0) {
                treatedAPDU.offset += 1
                print("\(TAG) handleEFResponse => \(apdu.name) READ 4")
                try self.read(
                    cardChannel: cardChannel,
                    apdu: apdu,
                    methodChannel: methodChannel
                )
            }
        } catch {
            throw error
        }
    }
    
    private func handleReadAPDUResponse(response: ResponseAPDU, status: APDUReadResponseEnum, apdu: ApduCommand, cardChannel: CardChannel, methodChannel: FlutterMethodChannel, getCardVersion: Bool) throws {
        let responseData: [UInt8] = response.data
        let responseHex: String = hexToBytesHelper.byteArrayToHexString(buffer: responseData)
        let totalBytes: Int = apdu.calculatedLength > 0 ? apdu.calculatedLength : apdu.lengthMin

        do {
            print("\(TAG) handleReadAPDUResponse => \(apdu.name) responseData length \(responseData.count)")

            if (apdu.name == "EF_IDENTIFICATION" && responseHex.count > 0 && countryCode.count < 1) {
                countryCode = countryCodeHelper.handleCountryCode(hex: responseHex)
                print("\(TAG) handleEFResponse => countryCode \(countryCode)")
            }

            if (totalBytes <= 255 && !apdu.isCertificat) {
                if (getCardVersion && apdu.name == "EF_APP_IDENTIFICATION") {
                    try setCardStructureVersionAndNoOfVariables(hexString: responseHex)
                } else if (!getCardVersion) {
                    try buildC1BFile(
                        hexString: responseHex,
                        apdu: apdu,
                        cardChannel: cardChannel,
                        methodChannel: methodChannel,
                        shouldWriteDataToFile: true
                    )
                }
            } else if (apdu.isCertificat) {
                try handleCertificateResponse(
                    responseHex: responseHex,
                    status: status,
                    apdu: apdu,
                    cardChannel: cardChannel,
                    methodChannel: methodChannel
                )
            } else {
                try handleEFResponse(
                    responseHex: responseHex,
                    apdu: apdu,
                    cardChannel: cardChannel,
                    methodChannel: methodChannel
                )
            }
        } catch {
            throw error
        }
    }
    
    private func buildC1BFile(hexString: String, apdu: ApduCommand, cardChannel: CardChannel, methodChannel: FlutterMethodChannel, shouldWriteDataToFile: Bool = false) throws {

        do {
            if (shouldWriteDataToFile && treatedAPDU.data.count == 0) {
                treatedAPDU.data = hexString
                try writeDataToC1BFile(
                    apdu: apdu,
                    methodChannel: methodChannel,
                    cardChannel: cardChannel,
                    needsSignature: apdu.needsSignature
                )
            } else if (shouldWriteDataToFile && treatedAPDU.data.count > 0) {
                treatedAPDU.data += " \(hexString)"
                try writeDataToC1BFile(
                    apdu: apdu,
                    methodChannel: methodChannel,
                    cardChannel: cardChannel,
                    needsSignature: apdu.needsSignature
                )
            } else if (treatedAPDU.data.count > 0) {
                treatedAPDU.data += " \(hexString)"
                
            }  else {
                treatedAPDU.data = hexString
            }
        } catch {
            throw error
        }
    }

    private func writeDataToC1BFile(apdu: ApduCommand, methodChannel: FlutterMethodChannel, cardChannel: CardChannel, needsSignature: Bool) throws {
        print("\(TAG) writeDataToC1BFile => \(treatedAPDU.name) data length ? \(hexToBytesHelper.cleanupHexString(hexString: treatedAPDU.data).count/2)")

        do {
            buildC1BDataKey(apdu: apdu)

            if (c1BFileData.count == 0) {
                c1BFileData += treatedAPDU.data
            } else {
                c1BFileData += " \(treatedAPDU.data)à"
            }

            if (needsSignature) {
                try performSign(cardChannel: cardChannel, methodChannel: methodChannel, apdu: apdu)
            }

            treatedAPDU = ApduData()
            uploadSteps += 1
            currentReadStepStatusNotifier.updateState(step: uploadSteps, channel: methodChannel)

            if (totalUploadSteps == uploadSteps) {
                c1BFileData += " "
                
                let contains: Bool = c1BFileData.contains("05 22 02 02 32")

                print("\(TAG) writeDataToC1BFile => Contains 05 22 02 02 32 \(contains)")
                dataTransferStateNotifier.updateState(state: DATA_TRANSFER_STATE_SUCCESS, channel: methodChannel)

                let md5HashKey: String = MD5Utils.encryptStr()
                let aesTrueString: String = try "vrai".encrypt(key: md5HashKey)
                let aesAgencyIdString: String = try driver!.agencyID.encrypt(key: md5HashKey)
                let aesDataString: String = try c1BFileData.encrypt(key: md5HashKey)

                let responseData: [String: Any] = [
                    "interim": aesTrueString,
                    "agencyID": aesAgencyIdString,
                    "fileData": aesDataString,
                    "countryCode": countryCode
                ]
            
                dataTransferNotifier.updateState(data: responseData, channel: methodChannel)
            }
        } catch {
            throw error
        }
    }
    
    private func performSign(cardChannel: CardChannel, methodChannel: FlutterMethodChannel, apdu: ApduCommand) throws {
        print("\(TAG) performSign => Perform Sign for \(apdu.name)")
        let TG1_SIGNATURE: String = "00 2A 9E 9A 80" // 128 bytes
        let TG2_SIGNATURE: String = "00 2A 9E 9A \(hexToBytesHelper.byteLength(apdu: nil, length: signatureLength))" // 64…132 bytes

        do {
            let readCommand = signatureVersion == CardGen.GEN1 ? TG1_SIGNATURE : TG2_SIGNATURE

            print("\(TAG) performSign => Trying to sign \(apdu.name) with command: \(readCommand)")

            let commandAPDU = try CommandAPDU(
                apdu: hexToBytesHelper.hexStringToByteArray(hexString: readCommand)
            )
            let response: ResponseAPDU = try cardChannel.transmit(apdu: commandAPDU)
            let sw1: UInt8? = response.sw1
            let sw2: UInt8? = response.sw2

            if (sw1 == nil && sw2 == nil) {
                print("\(TAG) performSign => Unable to sign APDU because sw1 and sw2 are NULL")
                throw ACSError.unableToSignApdu(description: UNABLE_TO_SIGN_APDU_EXCEPTION)
            } else {
                try handlePerformSignResponse(
                    response: response,
                    status: apduResponseHelper.signResponseIntToAPDUReadResponse(response: Int(sw1!)),
                    apdu: apdu,
                    cardChannel: cardChannel,
                    methodChannel: methodChannel
                )
            }
        } catch {
            throw error
        }

    }
    
    private func handlePerformSignResponse(response: ResponseAPDU, status: APDUSignResponseEnum, apdu: ApduCommand, cardChannel: CardChannel, methodChannel: FlutterMethodChannel) throws {
        print("\(TAG) handlePerformSignResponse => Perform sign status is \(status)")
        do {
            if (status == APDUSignResponseEnum.success) {
                let responseData: [UInt8] = response.data
                let responseHex: String = hexToBytesHelper.byteArrayToHexString(buffer: responseData)

                buildC1BDataKey(apdu: apdu, isSignature: true)

                c1BFileData += responseHex

            } else if (signatureLength >= maxSignatureLength) {
                throw ACSError.unableToSignApdu(description: UNABLE_TO_SIGN_APDU_EXCEPTION)
            } else {
                signatureLength += 1
                try performSign(
                    cardChannel: cardChannel,
                    methodChannel: methodChannel,
                    apdu: apdu
                )
            }
        } catch {
            throw error
        }
    }

    private func buildC1BDataKey(apdu: ApduCommand, isSignature: Bool = false) {
        var length: String = hexToBytesHelper.calculateLengthOfHex(hexString: treatedAPDU.data)

        if (isSignature && apdu.cardGen != CardGen.GEN1) {
            length = hexToBytesHelper.byteLength(apdu: nil, length: signatureLength)
        } else if (isSignature) {
            length = hexToBytesHelper.byteLength(apdu: nil, length: 128)
        }

        if (length.count == 2) {
            length = "00 \(length)"
        }

        print("\(TAG) buildC1BDataKey => Length for (SIGN ? \(isSignature) \(apdu.name) \(length)")

        if (isSignature) {
            c1BFileData += " \(apdu.hexNameSigned) \(length) "
        } else {
            let name = signatureVersion == CardGen.GEN1 ? apdu.hexName : apdu.hexNameGen2
            c1BFileData += " \(name) \(length)"
            print("\(TAG) buildC1BDataKey => \(name) \(length)")
        }
    }
    
    private func connectToCard(
        terminal: CardTerminal,
        methodChannel: FlutterMethodChannel
    ) {
        var card: Card
        var cardChannel: CardChannel

        print("\(TAG) - Connecting to card")
        cardConnectionStateNotifier.updateState(state: CARD_STATE_BONDING, channel: methodChannel)

        do {
            let protocolString: String = "*"
            card = try terminal.connect(protocolString: protocolString)
            
            print("\(TAG) - Connected to card")

            cardConnectionStateNotifier.updateState(state: CARD_STATE_CONNECTED, channel: methodChannel)

            cardChannel = try card.basicChannel()

            if (cardStructureVersion == nil) {
                try getCardVersion(cardChannel: cardChannel)
            }

            apduList = apduCommandListGenerator.makeList(cardGen: cardStructureVersion!, noOfVarModel: noOfVarModel)
            totalUploadSteps = apduCommandListGenerator.calculateTotalUploadSteps(apduList: apduList)
            totalReadStepsStatusNotifier.updateState(steps: totalUploadSteps, channel: methodChannel) // Remove MF
            dataTransferStateNotifier.updateState(state: DATA_TRANSFER_STATE_TRANSFERING, channel: methodChannel)
            try select(cardChannel: cardChannel)
            try disconnectCard(methodChannel: methodChannel, card: card)
            
        } catch {
            handleError(error: error.localizedDescription, methodChannel: methodChannel)
        }
    }
    
    private func performHashCommand(cardChannel: CardChannel) throws {
        let HASH_COMMAND: String = "80 2A 90 00"

        do {
            print("\(TAG) performHashCommand - Performing Hash command \(HASH_COMMAND)")

            let commandAPDU = try CommandAPDU(apdu: hexToBytesHelper.hexStringToByteArray(hexString: HASH_COMMAND))
            
            let response: ResponseAPDU = try cardChannel.transmit(apdu: commandAPDU)

            let sw1: UInt8? = response.sw1
            let sw2: UInt8? = response.sw2

            if (sw1 == nil && sw2 == nil) {

            } else {
                try handleHashAPDUResponse(
                    status: apduResponseHelper.hashResponseIntToAPDUReadResponse(response: Int(sw1!))
                )
            }
            
        } catch {
            throw error
        }
    }
    
    private func handleHashAPDUResponse(status: APDUHashResponseEnum) throws {
            if (status != APDUHashResponseEnum.success) {
                throw ACSError.unableToCreateHashException(description: UNABLE_TO_CREATE_HASH_EXCEPTION)
            }
    }
    
    private func disconnectCard(methodChannel: FlutterMethodChannel, card: Card? = nil) throws {
        do {
            if (card != nil) {
                try card!.disconnect(reset: true)
            }
            treatedAPDU = ApduData()
            c1BFileData = ""
            uploadSteps = 0
            totalUploadSteps = 0
            cardStructureVersion = nil
            noOfVarModel = NoOfVarModel()
            deviceConnectionStatusNotifier.updateState(state: CARD_STATE_DISCONNECTED, channel: methodChannel)
            cardConnectionStateNotifier.updateState(state: CARD_STATE_DISCONNECTED, channel: methodChannel)
        } catch {
            throw error
        }
    }
 }
