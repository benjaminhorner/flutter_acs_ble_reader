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
private var UNABLE_TO_SIGN_APDU_EXCEPTION = "Unable to Sign APDU"
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

class SmartCardReader
    (private val methodChannel: MethodChannel) {
    private lateinit var driver: Driver
    private lateinit var activity: Activity
    private var cardTerminalType: Int = 0
    private var mManager: BluetoothTerminalManager? = null
    private var mFactory: TerminalFactory? = null
    private var mHandler: Handler = Handler()
    private var cardStructureVersion: CardGen? = null
    private var noOfVarModel: NoOfVarModel = NoOfVarModel()
    private var totalUploadSteps: Int = 0
    private var uploadSteps: Int = 0
    private var c1BFileData: String = ""
    private var treatedAPDU = ApduData()
    private var tempOffset: Int = 0
    private var isEndOfData: Boolean = false
    private var apduList: List<ApduCommand> = listOf()
    private val maxSignatureLength: Int = 132
    private var signatureLength: Int = 64

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
            Log.e(TAG, "mManager cannot be null")
            deviceConnectionStatusNotifier.updateState("ERROR", methodChannel)
            return
        }

        mFactory = BluetoothSmartCard.getInstance(activity).getFactory()
        if (mFactory == null) {
            Log.e(TAG, "mFactory cannot be null")
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
                            Log.e(TAG, terminal.name)
                            connectToCard(terminal, methodChannel)
                        }
                    }
                }
            })
        } else {
            Log.e(TAG, "mManager cannot be null at this point")
            deviceConnectionStatusNotifier.updateState("ERROR", methodChannel)
            return
        }
        
        /* Stop the scan after a delay */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e(TAG, "stop scanning for devices…")
        }, (timeoutSeconds*1000).toLong())     
    }
    
    private fun connectToCard(
        terminal: CardTerminal,
        methodChannel: MethodChannel
    ) {
        var card: Card
        var cardChannel: CardChannel

        Log.e(TAG, "Connecting to card")
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
            totalUploadSteps = apduList.size - 2
            totalReadStepsStatusNotifier.updateState(totalUploadSteps, methodChannel) // Remove MF
            dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_TRANSFERING, methodChannel)
            select(cardChannel)
            disconnectCard(methodChannel, card)
            
        } catch (e: Exception) {
            handleError(e, methodChannel)
        }
    }

    private fun handleError(e: Exception, methodChannel: MethodChannel) {
        Log.e(TAG, "${e.message}")
        disconnectCard(methodChannel)
        dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_ERROR, methodChannel)
    }

    private fun handleSelectAPDUResponse(
        response: ResponseAPDU,
        status: APDUSelectResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        getCardVersion: Boolean,
        ) {
            try {
                val responseData: ByteArray = response.data
                val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)

                if (status != APDUSelectResponseEnum.SUCCESS) {
                    throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                }
                else if (apdu.isEF && apdu.needsHash) {
                    performHashCommand(cardChannel)
                    read(
                        cardChannel,
                        apdu,
                        methodChannel,
                        treatedAPDU.offset,
                        getCardVersion)
                } else if (apdu.isEF) {
                    read(
                        cardChannel,
                        apdu,
                        methodChannel,
                        treatedAPDU.offset,
                        getCardVersion)
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
                    Log.e(TAG, "Selecting APDU ${apdu.name} with command ${apdu.selectCommand}")

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
                            response, 
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
        remainingBytes: Int,
        ) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)

            Log.e(TAG, "${apdu.name} has status $status // remainingBytes: ${apdu.remainingBytes} // tempOffset $tempOffset // treatedAPDU.offset: ${treatedAPDU.offset}")

            if (status == APDUReadResponseEnum.SUCCESS) {
                treatedAPDU.length = 255
                if (getCardVersion && apdu.name == "EF_APP_IDENTIFICATION") {
                    setCardStructureVersionAndNoOfVariables(responseHex)
                } else if (!getCardVersion) {
                    buildC1BFile(
                        responseHex,
                        apdu,
                        cardChannel,
                        methodChannel)
                }
            } else if (
                status == APDUReadResponseEnum.P1_LENGTH_GREATER_THAN_EF || 
                status == APDUReadResponseEnum.P1_PLUS_LENGTH_GREATER_THAN_EF 
                && treatedAPDU.length > 0
                && apdu.isCertificat) {
                    isEndOfData = true
                    tempOffset = if (treatedAPDU.offset == 0) treatedAPDU.offset else treatedAPDU.offset - 1
                    if (treatedAPDU.length > apdu.lengthMin) {
                        treatedAPDU.length = apdu.lengthMin
                    } else {
                        treatedAPDU.length -= 1
                    }
                    read(
                        cardChannel,
                        apdu,
                        methodChannel,
                        tempOffset)
            } else if (
                status == APDUReadResponseEnum.P1_LENGTH_GREATER_THAN_EF || 
                status == APDUReadResponseEnum.P1_PLUS_LENGTH_GREATER_THAN_EF 
                && treatedAPDU.length > 0
                && !apdu.isCertificat) {
                    isEndOfData = true
                    tempOffset = if (treatedAPDU.offset == 0) treatedAPDU.offset else treatedAPDU.offset - 1
                    treatedAPDU.length = apdu.remainingBytes
                    read(
                        cardChannel,
                        apdu,
                        methodChannel,
                        tempOffset)
            } else {
                throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
            }
    }

    private fun read(
        cardChannel: CardChannel, 
        apdu: ApduCommand,
        methodChannel: MethodChannel,
        offset: Int = 0,
        getCardVersion: Boolean = false,
        ) {
            try {
                var p1: String = String.format("%02d", offset)
                var readCommand = "00 B0 ${p1} 00 ${hexToBytesHelper.byteLength(apdu, treatedAPDU.length)}"

                Log.e(TAG, "Reading APDU ${apdu.name}, offset value: $offset, command: ${readCommand}")

                val commandAPDU = CommandAPDU(
                    hexToBytesHelper.hexStringToByteArray(readCommand)
                )
                val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
                val sw1: Int? = response.getSW1()
                val sw2: Int? = response.getSW2()

                if (sw1 == null && sw2 == null) {
                    Log.e(TAG, "Unable to read card because sw1 and sw2 are NULL")
                    throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                } else {
                    handleReadAPDUResponse(
                        response,
                        apduResponseHelper.readResponseIntToAPDUReadResponse(sw1),
                        apdu,
                        cardChannel,
                        methodChannel,
                        getCardVersion,
                        sw2!!,
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
        ) {
            Log.e(TAG, "---------------------------------------------------")
            if (apdu.lengthMax <= 255) {
                writeDataToC1BFile(
                    hexString,
                    apdu,
                    methodChannel,
                    cardChannel,
                    apdu.needsSignature)
            } else if (apdu.name == treatedAPDU.name && !isEndOfData) {
                treatedAPDU.data += hexString
                if (tempOffset == 0) {
                    treatedAPDU.offset += 1
                    read(
                        cardChannel,
                        apdu,
                        methodChannel,
                        treatedAPDU.offset)
                }
            } else {
                writeDataToC1BFile(
                    hexString,
                    apdu,
                    methodChannel,
                    cardChannel,
                    apdu.needsSignature)
            }
    }

    private fun writeDataToC1BFile(
        hexString: String,
        apdu: ApduCommand,
        methodChannel: MethodChannel,
        cardChannel: CardChannel,
        needsSignature: Boolean
    ) {
        treatedAPDU.data = hexString
        treatedAPDU.offset = 0
        tempOffset = 0
        isEndOfData = false
        // c1BFileData += treatedAPDU.data
        c1BFileData += "${treatedAPDU.name} "
        uploadSteps += 1
        currentReadStepStatusNotifier.updateState(uploadSteps, methodChannel)

        if (needsSignature) {
            performSign(cardChannel, methodChannel, apdu, hexString)
        }

        if (totalUploadSteps == uploadSteps) {
            dataTransferStateNotifier.updateState(DATA_TRANSFER_STATE_SUCCESS, methodChannel)
            dataTransferNotifier.updateState(c1BFileData, methodChannel)
        }
        Log.e(TAG, "${treatedAPDU.name} uploadSteps: $uploadSteps")
        Log.e(TAG, "c1BFileData $c1BFileData")
        Log.e(TAG, "---------------------------------------------------")
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
            Log.e(TAG, "Performing Hash command ${HASH_COMMAND}")

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
        methodChannel: MethodChannel,
        apduResponse: String,
    ) {
        Log.e(TAG, "Perform sign status is $status")
        if (status == APDUSignResponseEnum.SUCCESS) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            c1BFileData += "SIGNED ${apdu.name} "
            Log.e(TAG, "Signature data is $responseHex")
        } else if (signatureLength >= maxSignatureLength) {
            throw Exception(UNABLE_TO_SIGN_APDU_EXCEPTION)
        } else {
            signatureLength += 1
            performSign(
                cardChannel,
                methodChannel,
                apdu,
                apduResponse)
        }
    }

    private fun performSign(
        cardChannel: CardChannel,
        methodChannel: MethodChannel, 
        apdu: ApduCommand,
        apduResponse: String,
    ) {
        Log.e(TAG, "Perform Sign for ${apdu.name}")
        // cardStructureVersion
        val TG1_SIGNATURE: String = "00 2A 9E 9A 80" // 128 bytes
        var TG2_SIGNATURE: String = "00 2A 9E 9A ${hexToBytesHelper.byteLength(null, signatureLength)}" // 64…132 bytes

        try {
            var readCommand = if (cardStructureVersion == CardGen.GEN1) TG1_SIGNATURE else TG2_SIGNATURE

            Log.e(TAG, "Trying to sign APDU with command: ${readCommand}")

            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(readCommand)
            )
            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
            val sw1: Int? = response.getSW1()
            val sw2: Int? = response.getSW2()

            if (sw1 == null && sw2 == null) {
                Log.e(TAG, "Unable to sign APDU because sw1 and sw2 are NULL")
                throw Exception(UNABLE_TO_SIGN_APDU_EXCEPTION)
            } else {
                handlePerformSignResponse(
                    response,
                    apduResponseHelper.signResponseIntToAPDUReadResponse(sw1),
                    apdu,
                    cardChannel,
                    methodChannel,
                    apduResponse,
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
    }
}
