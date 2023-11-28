package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.TotalReadStepsStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.CurrentReadStepStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.HexHelper
import com.benjamin.horner.flutter_acs_card_reader.CardConnectionStateNotifier
import com.benjamin.horner.flutter_acs_card_reader.DataTransferStateNotifier
import com.benjamin.horner.flutter_acs_card_reader.DataTransferNotifier
import com.benjamin.horner.flutter_acs_card_reader.ApduCommand
import com.benjamin.horner.flutter_acs_card_reader.CardGen
import com.benjamin.horner.flutter_acs_card_reader.ApduCommandListGenerator
import com.benjamin.horner.flutter_acs_card_reader.NoOfVarModel
import com.benjamin.horner.flutter_acs_card_reader.apduResponseHelper
import com.benjamin.horner.flutter_acs_card_reader.APDUSelectResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUReadResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUHashResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUSignResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.ApduData
import com.benjamin.horner.flutter_acs_card_reader.MD5Utils
import com.benjamin.horner.flutter_acs_card_reader.AESUtils

/// Flutter
import io.flutter.plugin.common.MethodChannel

/// ACS
import com.acs.smartcardio.BluetoothSmartCard
import com.acs.smartcardio.BluetoothTerminalManager
import com.acs.smartcardio.TerminalTimeouts
import com.acs.smartcardio.TransmitOptions

/// Android
import android.content.Context
import android.app.Activity
import android.util.Log
import android.os.Handler

/// JavaX
import javax.smartcardio.TerminalFactory
import javax.smartcardio.CardTerminal
import javax.smartcardio.CardChannel
import javax.smartcardio.CommandAPDU
import javax.smartcardio.ResponseAPDU
import javax.smartcardio.CardException
import javax.smartcardio.Card
import javax.smartcardio.ATR

private val TAG = "SmartCardReader"
private val UNABLE_TO_TRANSMIT_APDU_EXCEPTION = "Unable to transmit APDU"
private val UNABLE_TO_CREATE_HASH_EXCEPTION = "Unable to Create Hash"
private val UNABLE_TO_PERFORM_SELECTION = "Unable to select from card"
private val UNABLE_TO_CONNECT_TO_CARD = "Unable to connect to card"
private val HEX_DOES_NOT_CONTAIN_ENOUGH_BYTES = "Hex string does not contain enough bytes."
private val DATA_TRANSFER_STATE_PENDING = "PENDING"
private val DATA_TRANSFER_STATE_TRANSFERING = "TRANSFERING"
private val DATA_TRANSFER_STATE_SUCCESS = "SUCCESS"
private val DATA_TRANSFER_STATE_ERROR = "ERROR"
private val UNABLE_TO_SIGN_APDU_EXCEPTION = "Unable to Sign APDU"
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private val deviceNotifier = DeviceNotifier()
private val hexToBytesHelper = HexHelper()
private val cardConnectionStateNotifier = CardConnectionStateNotifier()
private val totalReadStepsStatusNotifier = TotalReadStepsStatusNotifier()
private val currentReadStepStatusNotifier = CurrentReadStepStatusNotifier()
private val dataTransferStateNotifier = DataTransferStateNotifier()
private val apduCommandListGenerator = ApduCommandListGenerator()
private val apduResponseHelper = APDUResponseHelper()
private val dataTransferNotifier = DataTransferNotifier()
private val logDataNotifier = LogDataNotifier()
private val countryCodeHelper = CountryCodeHelper()

class SmartCardReader
    (private val methodChannel: MethodChannel) {
    private lateinit var driver: Driver
    private lateinit var activity: Activity
    private var cardTerminalType: Int = 0
    private var mManager: BluetoothTerminalManager? = null
    private var mFactory: TerminalFactory? = null
    private var mHandler: Handler = Handler()
    private var cardStructureVersion: CardGen? = null
    private var signatureVersion: CardGen? = null
    private var noOfVarModel: NoOfVarModel = NoOfVarModel()
    private var totalUploadSteps: Int = 0
    private var uploadSteps: Int = 0
    private var c1BFileData: String = ""
    private var treatedAPDU = ApduData()
    private var apduList: List<ApduCommand> = listOf()
    private val maxSignatureLength: Int = 132
    private var signatureLength: Int = 64
    private var countryCode: String = ""
    private var logData: String = ""

    fun connectToDevice(
        activity: Activity, 
        context: Context, 
        driver: Driver, 
        cardTerminalType: Int, 
        timeoutSeconds: Int,
    ) {
        this.activity = activity
        this.driver = driver
        this.cardTerminalType = cardTerminalType

        mManager = BluetoothSmartCard.getInstance(activity).getManager()
        if (mManager == null) {
            Log.e("$TAG connectToDevice", "mManager cannot be null")
            logData += "[ERROR]/[SmartCardReader.kt]/[connectToDevice] : mManager cannot be null\n"
            deviceConnectionStatusNotifier.updateState("ERROR", methodChannel)
            return
        }

        mFactory = BluetoothSmartCard.getInstance(activity).getFactory()
        if (mFactory == null) {
            Log.e("$TAG connectToDevice", "mFactory cannot be null")
            logData += "[ERROR]/[SmartCardReader.kt]/[connectToDevice] : mFactory cannot be null\n"
            deviceConnectionStatusNotifier.updateState("ERROR", methodChannel)
            return
        }

        startScan(timeoutSeconds)

    }

    private fun startScan(timeoutSeconds: Int) {
        if (mManager != null) {
            mManager!!.startScan(cardTerminalType, object : BluetoothTerminalManager.TerminalScanCallback {
                override fun onScan(terminal: CardTerminal) {
                    if (terminal.name.contains("ACR")) {
                        mManager?.stopScan()
                        deviceNotifier.updateState(terminal, methodChannel)
                        deviceConnectionStatusNotifier.updateState("CONNECTED", methodChannel)
                        activity.runOnUiThread {
                            logData += "[ERROR]/[SmartCardReader.kt]/[startScan] : Terminal name ${terminal.name}\n"
                            Log.e("$TAG startScan", terminal.name)
                            connectToCard(terminal, methodChannel)
                        }
                    }
                }
            })
        } else {
            Log.e("$TAG startScan", "mManager cannot be null at this point")
            logData += "[ERROR]/[SmartCardReader.kt]/[startScan] : mManager cannot be null at this point\n"
            deviceConnectionStatusNotifier.updateState("ERROR", methodChannel)
            return
        }
        
        /* Stop the scan after a delay */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e("$TAG startScan", "stop scanning for devices…")
        }, (timeoutSeconds*1000).toLong())     
    }
    
    private fun connectToCard(
        terminal: CardTerminal,
        methodChannel: MethodChannel
    ) {
        var card: Card
        var cardChannel: CardChannel

        Log.e("$TAG connectToCard", "Connecting to card")
        cardConnectionStateNotifier.updateState("BONDING", methodChannel)

        try {
            card = terminal.connect("*")
            val atr: ATR = card.atr
            val atrBytes: ByteArray = atr.bytes
            val atrHex: String = hexToBytesHelper.byteArrayToHexString(atrBytes)

            cardConnectionStateNotifier.updateState("CONNECTED", methodChannel)

            cardChannel = card.basicChannel

            if (cardStructureVersion == null) {
                getCardVersion(cardChannel)
            }

            apduList = apduCommandListGenerator.makeList(cardStructureVersion!!, noOfVarModel)
            totalUploadSteps = apduCommandListGenerator.calculateTotalUploadSteps(apduList)
            totalReadStepsStatusNotifier.updateState(totalUploadSteps, methodChannel) // Remove MF
            dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_TRANSFERING, methodChannel)
            select(cardChannel)
            disconnectCard(methodChannel, card)
            
        } catch (e: Exception) {
            handleError(e, methodChannel)
        }
    }

    private fun handleError(e: Exception, methodChannel: MethodChannel) {
        logData += "[ERROR]/[SmartCardReader.kt]/[handleError] : ${e.message}\n"
        Log.e("$TAG handleError", "${e.message}")
        disconnectCard(methodChannel)
        dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_ERROR, methodChannel)
        currentReadStepStatusNotifier.updateState(uploadSteps, methodChannel)
    }

    private fun handleSelectAPDUResponse(
        status: APDUSelectResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        getCardVersion: Boolean,
        ) {
            try {
                if (status != APDUSelectResponseEnum.SUCCESS) {
                    throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                }
                else if (apdu.isEF && apdu.needsHash) {
                    performHashCommand(cardChannel)
                    Log.e("$TAG handleSelectAPDUResponse", "${apdu.name} READ 1")
                    read(
                        cardChannel = cardChannel,
                        apdu = apdu,
                        methodChannel = methodChannel,
                        getCardVersion = getCardVersion,
                    )
                } else if (apdu.isEF) {
                    Log.e("$TAG handleSelectAPDUResponse", "${apdu.name} READ 2")
                    read(
                        cardChannel = cardChannel,
                        apdu = apdu,
                        methodChannel = methodChannel,
                        getCardVersion = getCardVersion,
                    )
                } else if (apdu.name.contains("DF")) {
                    signatureVersion = if (apdu.name.contains("G2")) CardGen.GEN2 else CardGen.GEN1
                }
            }
            catch (e: Exception) {
                throw e
            }
    }

    private fun select(
        cardChannel: CardChannel,
        getCardVersion: Boolean = false,
        ) {
            try {
                for (apdu in apduList) {
                    
                    Log.e("$TAG select", "Selecting APDU ${apdu.name} with command ${apdu.selectCommand}")

                    treatedAPDU.name = apdu.name

                    val commandAPDU = CommandAPDU(
                        hexToBytesHelper.hexStringToByteArray(apdu.selectCommand)
                    )
                    val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
                    val sw1: Int? = response.getSW1()
                    val sw2: Int? = response.getSW2()

                    if (sw1 == null && sw2 == null) {
                        throw Exception(UNABLE_TO_PERFORM_SELECTION)
                        break
                    } else {
                        handleSelectAPDUResponse(
                            apduResponseHelper.selectResponseIntToAPDUReadResponse(sw1),
                            apdu,
                            cardChannel,
                            getCardVersion,
                        )
                    }
                }
            }
            catch (e: Exception) {
                throw e
            }
    }

    private fun handleReadAPDUResponse(
        response: ResponseAPDU,
        status: APDUReadResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        methodChannel: MethodChannel,
        getCardVersion: Boolean,
        ) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            val totalBytes: Int = if (apdu.calculatedLength > 0) apdu.calculatedLength else apdu.lengthMin

            Log.e("$TAG handleReadAPDUResponse", "${apdu.name} responseData Length ${responseData.size}")

            if (apdu.name == "EF_IDENTIFICATION" && responseHex.length > 0 && countryCode.length < 1) {
                countryCode = countryCodeHelper.handleCountryCode(responseHex)
                Log.e("$TAG handleEFResponse", "countryCode $countryCode")
            }

            if (totalBytes <= 255 && !apdu.isCertificat) {
                if (getCardVersion && apdu.name == "EF_APP_IDENTIFICATION") {
                    setCardStructureVersionAndNoOfVariables(responseHex)
                } else if (!getCardVersion) {
                    buildC1BFile(
                        responseHex,
                        apdu,
                        cardChannel,
                        methodChannel,
                        true,
                    )
                }
            } else if (apdu.isCertificat) {
                handleCertificateResponse(
                    responseHex = responseHex,
                    status = status,
                    apdu,
                    cardChannel = cardChannel,
                    methodChannel = methodChannel,
                )
            } else {
                handleEFResponse(
                    responseHex = responseHex,
                    apdu = apdu,
                    cardChannel = cardChannel,
                    methodChannel = methodChannel,
                )
            }
    }

    private fun handleCertificateResponse(
        responseHex: String,
        status: APDUReadResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        methodChannel: MethodChannel,
    ) {
        Log.e("$TAG handleCertificateResponse", "${apdu.name} status $status")

        buildC1BFile(
            hexString = responseHex,
            apdu = apdu,
            cardChannel = cardChannel,
            methodChannel = methodChannel,
            shouldWriteDataToFile = status != APDUReadResponseEnum.SUCCESS
        )
        if (
            status == APDUReadResponseEnum.SUCCESS) {
                Log.e("$TAG handleCertificateResponse", "${apdu.name} READ 3")
                treatedAPDU.offset += 1
                read(
                    cardChannel,
                    apdu,
                    methodChannel,
                )
        }
    }

    private fun handleEFResponse(
        responseHex: String,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        methodChannel: MethodChannel,
    ) {
        val totalBytesLength: Int = hexToBytesHelper.cleanupHexString(treatedAPDU.data).length/2 + hexToBytesHelper.cleanupHexString(responseHex).length/2

        Log.e("$TAG handleEFResponse", "${apdu.name} totalBytesLength == apdu.calculatedLength ? ${totalBytesLength == apdu.calculatedLength} totalBytesLength $totalBytesLength apdu.calculatedLength ${apdu.calculatedLength}")

        buildC1BFile(
                responseHex,
                apdu,
                cardChannel,
                methodChannel,
                totalBytesLength == apdu.calculatedLength
            )

        if (treatedAPDU.offset <= apdu.maxReadLoops && treatedAPDU.name.length > 0) {
            treatedAPDU.offset++
            Log.e("$TAG handleEFResponse", "${apdu.name} READ 4")
            read(
                cardChannel = cardChannel,
                apdu = apdu,
                methodChannel = methodChannel,
            )
        }
    }

    private fun calculateOffset(apdu: ApduCommand): String {
        var hexString: String = ""
        if (apdu.isCertificat) {
            hexString = if (treatedAPDU.offset > 0) Integer.toHexString(203 + treatedAPDU.offset) else "00"
        } else {
            hexString = Integer.toHexString(treatedAPDU.offset * 255)
        }
        return hexToBytesHelper.padHex(hexString = hexString, desiredLength = 4)
    }

    private fun calculateExpectedLength(apdu: ApduCommand): String {
        if (apdu.isCertificat) {
            val bytes: Int = if (treatedAPDU.offset > 0) 1 else 204
            return hexToBytesHelper.byteLength(null, bytes)
        } else {
            val bytes: Int = if (apdu.remainingBytes > 0 ) apdu.remainingBytes else apdu.lengthMin
            if (treatedAPDU.offset >= apdu.maxReadLoops || apdu.maxReadLoops == 0){
                return hexToBytesHelper.byteLength(null, bytes)
            } else {
                return "FF"
            }
        }
    }

    private fun buildreadCommand(apdu: ApduCommand): String {
        var readCommand = "00 B0 ${calculateOffset(apdu)} ${calculateExpectedLength(apdu)}"
        Log.e("$TAG read", "Reading APDU ${apdu.name}, command: ${readCommand}")
        return readCommand
    }

    private fun read(
        cardChannel: CardChannel, 
        apdu: ApduCommand,
        methodChannel: MethodChannel,
        getCardVersion: Boolean = false,
        ) {
            try {
                val commandAPDU = CommandAPDU(
                    hexToBytesHelper.hexStringToByteArray(buildreadCommand(apdu))
                )
                val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
                val sw1: Int? = response.getSW1()
                val sw2: Int? = response.getSW2()

                if (sw1 == null && sw2 == null) {
                    Log.e("$TAG read", "Unable to read card because sw1 and sw2 are NULL")
                    throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                } else {
                    handleReadAPDUResponse(
                        response,
                        apduResponseHelper.readResponseIntToAPDUReadResponse(sw1),
                        apdu,
                        cardChannel,
                        methodChannel,
                        getCardVersion,
                    )
                }
            } catch (e: Exception) {
                throw e
            }
    }

    private fun getCardVersion(cardChannel: CardChannel, testGen1: Boolean = false) {
        try {
            apduList = if (testGen1) apduCommandListGenerator.cardVersionCommandList() else apduCommandListGenerator.cardVersionGen2CommandList()
            select(cardChannel, true)
        }
        catch (e: Exception) {
            if (e.message == UNABLE_TO_TRANSMIT_APDU_EXCEPTION) {
                getCardVersion(cardChannel, true)
            }
            else {
                throw e
            }
        }
    }

    private fun setCardStructureVersionAndNoOfVariables(hexString: String) {
        val hexValues = hexString.split(" ")
        
        if (hexValues.size >= 17) { // Gen 2 Case
            setCardGenerationAndVersion(hexValues[1], hexValues[2])

            noOfVarModel.noOfEventsPerType = hexValues[3].toInt(16)
            noOfVarModel.noOfFaultsPerType = hexValues[4].toInt(16)
            noOfVarModel.cardActivityLengthRange = (hexValues[5] + hexValues[6]).toInt(16)
            noOfVarModel.noOfCardVehicleRecords = (hexValues[7] + hexValues[8]).toInt(16)
            noOfVarModel.noOfCardPlaceRecords = (hexValues[9] + hexValues[10]).toInt(16)
            noOfVarModel.noOfGNSSRecords = (hexValues[11] + hexValues[12]).toInt(16)
            noOfVarModel.noOfSpecificConditionsRecords = (hexValues[13] + hexValues[14]).toInt(16)
            noOfVarModel.noOfCardVehicleUnitRecords = (hexValues[15] + hexValues[16]).toInt(16)

            Log.e(TAG, "Card Structure Hex is: $hexString")
            Log.e(TAG, "Card Structure Card Generation is: ${hexValues[1]}")
            Log.e(TAG, "Card Structure Card version number is: ${hexValues[2]}")
            Log.e(TAG, "Card Structure noOfEventsPerType is: ${noOfVarModel.noOfEventsPerType}")
            Log.e(TAG, "Card Structure noOfFaultsPerType is: ${noOfVarModel.noOfFaultsPerType}")
            Log.e(TAG, "Card Structure cardActivityLengthRange is: ${noOfVarModel.cardActivityLengthRange}")
            Log.e(TAG, "Card Structure noOfCardVehicleRecords is: ${noOfVarModel.noOfCardVehicleRecords}")
            Log.e(TAG, "Card Structure noOfCardPlaceRecords is: ${noOfVarModel.noOfCardPlaceRecords}")
            Log.e(TAG, "Card Structure noOfGNSSRecords is: ${noOfVarModel.noOfGNSSRecords}")
            Log.e(TAG, "Card Structure noOfCardVehicleUnitRecords is: ${noOfVarModel.noOfCardVehicleUnitRecords}")
            Log.e(TAG, "Card Structure noOfSpecificConditionsRecords is: ${noOfVarModel.noOfSpecificConditionsRecords}")

        } else if (hexValues.size == 10) { // Gen 1 Case
        val generationHex = hexValues[1]
            setCardGenerationAndVersion(hexValues[1], hexValues[2])

            noOfVarModel.noOfEventsPerType = hexValues[3].toInt(16)
            noOfVarModel.noOfFaultsPerType = hexValues[4].toInt(16)
            noOfVarModel.cardActivityLengthRange = (hexValues[5] + hexValues[6]).toInt(16)
            noOfVarModel.noOfCardVehicleRecords = (hexValues[7] + hexValues[8]).toInt(16)
            noOfVarModel.noOfCardPlaceRecords = hexValues[9].toInt(16)

            Log.e(TAG, "Card Structure Hex is: $hexString")
            Log.e(TAG, "Card Structure Card Generation is: ${hexValues[1]}")
            Log.e(TAG, "Card Structure Card version number is: ${hexValues[2]}")
            Log.e(TAG, "Card Structure noOfEventsPerType is: ${noOfVarModel.noOfEventsPerType}")
            Log.e(TAG, "Card Structure noOfFaultsPerType is: ${noOfVarModel.noOfFaultsPerType}")
            Log.e(TAG, "Card Structure cardActivityLengthRange is: ${noOfVarModel.cardActivityLengthRange}")
            Log.e(TAG, "Card Structure noOfCardVehicleRecords is: ${noOfVarModel.noOfCardVehicleRecords}")
            Log.e(TAG, "Card Structure noOfCardPlaceRecords is: ${noOfVarModel.noOfCardPlaceRecords}")
            

        } else if (hexValues.size >= 3) { // Card Generation & Version
            Log.e(TAG, "Card Structure Hex is: $hexString")
            Log.e(TAG, "Card Structure Card Generation is: ${hexValues[1]}")
            Log.e(TAG, "Card Structure Card version number is: ${hexValues[2]}")

            setCardGenerationAndVersion(hexValues[1], hexValues[2])
        }
        else {
            Log.e(TAG, "Card Structure Hex is: $hexString")
            throw Exception(HEX_DOES_NOT_CONTAIN_ENOUGH_BYTES)
        }
    }

    private fun setCardGenerationAndVersion(generationHex: String, versionHex: String) {
        if (generationHex == "00") {
            cardStructureVersion = CardGen.GEN1
        } else if (generationHex == "01" && versionHex == "00") {
            cardStructureVersion = CardGen.GEN2
        } else {
            cardStructureVersion = CardGen.GEN2V2
        }
    }

    private fun buildC1BFile(
        hexString: String,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        methodChannel: MethodChannel,
        shouldWriteDataToFile: Boolean = false,
        ) {

            if (shouldWriteDataToFile && treatedAPDU.data.length == 0) {
                treatedAPDU.data = hexString
                writeDataToC1BFile(
                    apdu,
                    methodChannel,
                    cardChannel,
                    apdu.needsSignature,
                )
            } else if (shouldWriteDataToFile && treatedAPDU.data.length > 0) {
                treatedAPDU.data += " $hexString"
                writeDataToC1BFile(
                    apdu,
                    methodChannel,
                    cardChannel,
                    apdu.needsSignature,
                )
            } else if (treatedAPDU.data.length > 0) {
                treatedAPDU.data += " $hexString"
                
            }  else {
                treatedAPDU.data = hexString
            }
    }

    private fun writeDataToC1BFile(
        apdu: ApduCommand,
        methodChannel: MethodChannel,
        cardChannel: CardChannel,
        needsSignature: Boolean
    ) {
        Log.e("$TAG writeDataToC1BFile", "${treatedAPDU.name} data length ? ${hexToBytesHelper.cleanupHexString(treatedAPDU.data).length/2}")

        buildC1BDataKey(apdu)

        if (c1BFileData.length == 0) {
            c1BFileData += treatedAPDU.data
        } else {
            c1BFileData += " ${treatedAPDU.data}"
        }

        if (needsSignature) {
            performSign(cardChannel, methodChannel, apdu)
        }

        treatedAPDU = ApduData()
        uploadSteps += 1
        currentReadStepStatusNotifier.updateState(uploadSteps, methodChannel)

        if (totalUploadSteps == uploadSteps) {
            c1BFileData += " "
            
            val contains: Boolean = c1BFileData.contains("05 22 02 02 32")

            Log.e("$TAG writeDataToC1BFile", "Contains 05 22 02 02 32 $contains")
            dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_SUCCESS, methodChannel)

            val md5HashKey: String = MD5Utils.encryptStr()
            val aesTrueString: String = AESUtils.encrypt("vrai", md5HashKey)
            val aesAgencyIdString: String = AESUtils.encrypt(driver.agencyID!!, md5HashKey)
            val aesDataString: String = AESUtils.encrypt(c1BFileData, md5HashKey)

            val responseData: Map<String, Any> = mapOf(
                "interim" to aesTrueString,
                "agencyID" to aesAgencyIdString,
                "fileData" to aesDataString,
                "countryCode" to countryCode
            )
        
            dataTransferNotifier.updateState(responseData, methodChannel)
        }
    }

    private fun buildC1BDataKey(
        apdu: ApduCommand,
        isSignature: Boolean = false,
    ) {
        var length: String = hexToBytesHelper.calculateLengthOfHex(treatedAPDU.data)

        if (isSignature && apdu.cardGen != CardGen.GEN1) {
            length = hexToBytesHelper.byteLength(null, signatureLength)
        } else if (isSignature) {
            length = hexToBytesHelper.byteLength(null, 128)
        }

        if (length.length == 2) {
            length = "00 $length"
        }

        Log.e("$TAG buildC1BDataKey", "Length for (SIGN. ? $isSignature) ${apdu.name} ${length}")

        if (isSignature) {
            c1BFileData += " ${apdu.hexNameSigned} $length "
        } else {
            var name = if (signatureVersion == CardGen.GEN1) apdu.hexName else apdu.hexNameGen2
            c1BFileData += " $name $length"
            Log.e("$TAG buildC1BDataKey", "$name $length")
        }
    }

    private fun handleHashAPDUResponse(
        status: APDUHashResponseEnum
        ) {
            if (status != APDUHashResponseEnum.SUCCESS) {
                throw Exception(UNABLE_TO_CREATE_HASH_EXCEPTION)
            }
    }

    private fun performHashCommand(cardChannel: CardChannel) {
        val HASH_COMMAND: String = "80 2A 90 00"

        try {
            Log.e("$TAG performHashCommand", "Performing Hash command ${HASH_COMMAND}")

            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(HASH_COMMAND)
            )
            
            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)

            val sw1: Int? = response.getSW1()
            val sw2: Int? = response.getSW2()

            if (sw1 == null && sw2 == null) {

            } else {
                handleHashAPDUResponse(
                    apduResponseHelper.hashResponseIntToAPDUReadResponse(sw1!!)
                )
            }
            
        } catch (e: Exception) {
            throw e
        }
    }

    private fun handlePerformSignResponse(
        response: ResponseAPDU,
        status: APDUSignResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        methodChannel: MethodChannel
    ) {
        Log.e("$TAG handlePerformSignResponse", "Perform sign status is $status")
        if (status == APDUSignResponseEnum.SUCCESS) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)

            buildC1BDataKey(apdu, true)

            c1BFileData += responseHex

        } else if (signatureLength >= maxSignatureLength) {
            throw Exception(UNABLE_TO_SIGN_APDU_EXCEPTION)
        } else {
            signatureLength += 1
            performSign(
                cardChannel,
                methodChannel,
                apdu,
                )
        }
    }

    private fun performSign(
        cardChannel: CardChannel,
        methodChannel: MethodChannel, 
        apdu: ApduCommand,
    ) {
        Log.e("$TAG performSign", "Perform Sign for ${apdu.name}")
        val TG1_SIGNATURE: String = "00 2A 9E 9A 80" // 128 bytes
        var TG2_SIGNATURE: String = "00 2A 9E 9A ${hexToBytesHelper.byteLength(null, signatureLength)}" // 64…132 bytes

        try {
            var readCommand = if (signatureVersion == CardGen.GEN1) TG1_SIGNATURE else TG2_SIGNATURE

            Log.e("$TAG performSign", "Trying to sign ${apdu.name} with command: ${readCommand}")

            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(readCommand)
            )
            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
            val sw1: Int? = response.getSW1()
            val sw2: Int? = response.getSW2()

            if (sw1 == null && sw2 == null) {
                Log.e("$TAG performSign", "Unable to sign APDU because sw1 and sw2 are NULL")
                throw Exception(UNABLE_TO_SIGN_APDU_EXCEPTION)
            } else {
                handlePerformSignResponse(
                    response,
                    apduResponseHelper.signResponseIntToAPDUReadResponse(sw1),
                    apdu,
                    cardChannel,
                    methodChannel,
                )
            }
        } catch (e: Exception) {
            throw e
        }

    }

    private fun disconnectCard(
        methodChannel: MethodChannel,
        card: Card? = null
        ) {
        if (card != null) {
            card.disconnect(true)
        }
        treatedAPDU = ApduData()
        c1BFileData = ""
        uploadSteps = 0
        totalUploadSteps = 0
        cardStructureVersion = null
        noOfVarModel = NoOfVarModel()
        deviceConnectionStatusNotifier.updateState("DISCONNECTED", methodChannel)
        cardConnectionStateNotifier.updateState("DISCONNECTED", methodChannel)
        logDataNotifier.updateState(logData, methodChannel)
        logData = ""
    }
}
