package com.benjamin.horner.flutter_acs_card_reader

/// Local
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.benjamin.horner.flutter_acs_card_reader.DeviceNotifier
import com.benjamin.horner.flutter_acs_card_reader.TotalReadStepsStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.CurrentReadStepStatusNotifier
import com.benjamin.horner.flutter_acs_card_reader.HexHelper
import com.benjamin.horner.flutter_acs_card_reader.CardConnectionStateNotifier
import com.benjamin.horner.flutter_acs_card_reader.ApduCommand
import com.benjamin.horner.flutter_acs_card_reader.CardGen
import com.benjamin.horner.flutter_acs_card_reader.ApduCommandListGenerator
import com.benjamin.horner.flutter_acs_card_reader.NoOfVarModel
import com.benjamin.horner.flutter_acs_card_reader.APDUResponseHelper
import com.benjamin.horner.flutter_acs_card_reader.APDUSelectResponseEnum
import com.benjamin.horner.flutter_acs_card_reader.APDUReadResponseEnum

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
private val deviceConnectionStatusNotifier = DeviceConnectionStatusNotifier()
private val deviceNotifier = DeviceNotifier()
private val hexToBytesHelper = HexHelper()
private val cardConnectionStateNotifier = CardConnectionStateNotifier()
private val totalReadStepsStatusNotifier = TotalReadStepsStatusNotifier()
private val currentReadStepStatusNotifier = CurrentReadStepStatusNotifier()
private val apduCommandListGenerator = ApduCommandListGenerator()
private val aPDUResponseHelper = APDUResponseHelper()

class SmartCardReader
    (private val channel: MethodChannel) {
    private lateinit var driver: Driver
    private lateinit var activity: Activity
    private var cardTerminalType: Int = 0
    private var mManager: BluetoothTerminalManager? = null
    private var mFactory: TerminalFactory? = null
    private var mHandler: Handler = Handler()
    private var cardStructureVersion: CardGen? = null
    private var noOfVarModel: NoOfVarModel = NoOfVarModel()
    private var uploadSteps: Int = 0

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
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }

        mFactory = BluetoothSmartCard.getInstance(activity).getFactory()
        if (mFactory == null) {
            Log.e(TAG, "mFactory cannot be null")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }

        /// Start scanning for devices
        startScan(timeoutSeconds)

    }

    private fun startScan(timeoutSeconds: Int) {
        // TODO: Check if a Terminal is already connected
        // If the terminal is already connected, break and read from the connected terminal
        Log.e(TAG, "Scanning for devices…")

        if (mManager != null) {
            mManager!!.startScan(cardTerminalType, object : BluetoothTerminalManager.TerminalScanCallback {
                override fun onScan(terminal: CardTerminal) {
                    if (terminal.name.contains("ACR")) {
                        mManager?.stopScan()
                        deviceNotifier.updateState(terminal, channel)
                        deviceConnectionStatusNotifier.updateState("CONNECTED", channel)
                        activity.runOnUiThread {
                            Log.e(TAG, terminal.name)
                            connectToCard(terminal, channel)
                        }
                    }
                }
            })
        } else {
            Log.e(TAG, "mManager cannot be null at this point")
            deviceConnectionStatusNotifier.updateState("ERROR", channel)
            return
        }
        
        /* Stop the scan. */
        mHandler.postDelayed({
            mManager?.stopScan()
            Log.e(TAG, "stop scanning for devices…")
        }, (timeoutSeconds*1000).toLong())     
    }
    
    private fun connectToCard(terminal: CardTerminal, channel: MethodChannel) {
        var card: Card
        var cardChannel: CardChannel

        Log.e(TAG, "Connecting to card")
        cardConnectionStateNotifier.updateState("BONDING", channel)

        try {
            card = terminal.connect("*")
            val atr: ATR = card.atr
            val atrBytes: ByteArray = atr.bytes
            val atrHex: String = hexToBytesHelper.byteArrayToHexString(atrBytes)

            Log.e(TAG, "ATR is: ${atrHex}")
            Log.e(TAG, "Established connection to card!")

            cardConnectionStateNotifier.updateState("CONNECTED", channel)

            cardChannel = card.basicChannel

            if (cardStructureVersion == null) {
                getCardVersion(cardChannel)
            }

            Log.e(TAG, "Card version is: ${cardStructureVersion}")

            val apduList: List<ApduCommand> = apduCommandListGenerator.makeList(cardStructureVersion!!, noOfVarModel)

            totalReadStepsStatusNotifier.updateState(apduList.size - 1, channel)

            select(cardChannel, apduList)
            
            disconnectCard(channel, card)
            
        } catch (e: CardException) {
            Log.e(TAG, "Unable to connect to card")
            disconnectCard(channel)
            e.printStackTrace()
            // TODO: Handle error
        }
    }

    private fun handleSelectAPDUResponse(
        response: ResponseAPDU,
        status: APDUSelectResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        getCardVersion: Boolean,
        ) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            val responseDataToString: String = hexToBytesHelper.convertHexToASCII(responseHex)

            if (status == APDUSelectResponseEnum.SUCCESS) {
                if (apdu.isEF) {
                    performHashCommand(cardChannel)
                    loopOrRead(cardChannel, apdu, getCardVersion)
                }
            } 
            else {
                Log.e(TAG, "Unable to transmit APDU. sw1 was $status")
                throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
            }
    }

    private fun select(
        cardChannel: CardChannel,
        apduList: List<ApduCommand>,
        getCardVersion: Boolean = false,
        ) {
            try {
                for (apdu in apduList) {
                    Log.e(TAG, "Selecting APDU ${apdu.name} with command ${apdu.selectCommand}")

                    val commandAPDU = CommandAPDU(
                        hexToBytesHelper.hexStringToByteArray(apdu.selectCommand)
                    )
                    val response: ResponseAPDU = cardChannel.transmit(commandAPDU)
                    val sw1: Int? = response.getSW1()
                    val sw2: Int? = response.getSW2()

                    if (sw1 == null && sw2 == null) {
                        Log.e(TAG, "Unable to transmit APDU. sw1 was $sw1 and sw2 was $sw2")
                        throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
                        break
                    } else {
                        handleSelectAPDUResponse(
                            response, 
                            aPDUResponseHelper.selectResponseIntToAPDUReadResponse(sw1),
                            apdu,
                            cardChannel,
                            getCardVersion,
                        )
                    }
                }
            }
            catch(e: Exception) {
                Log.e(TAG, "Unable to select from card")
                e.printStackTrace()
                throw e
            }
    }

    private fun handleReadAPDUResponse(
        response: ResponseAPDU,
        status: APDUReadResponseEnum,
        apdu: ApduCommand,
        cardChannel: CardChannel,
        getCardVersion: Boolean,
        remainingBytes: Int,
        ) {
            val responseData: ByteArray = response.data
            val responseHex: String = hexToBytesHelper.byteArrayToHexString(responseData)
            val responseDataToString: String = hexToBytesHelper.convertHexToASCII(responseHex)

            if (status == APDUReadResponseEnum.SUCCESS) {
                Log.e(TAG, "APDU Read name was ${apdu.name}")
                Log.e(TAG, "APDU Read Response data size: ${responseData.size}")
                Log.e(TAG, "APDU Read Response Hex: ${responseHex}")
                if (getCardVersion && apdu.name == "EF_APP_IDENTIFICATION") {
                    setCardStructureVersionAndNoOfVariables(responseHex)
                } else {
                    addToC1BFile(responseHex, apdu)
                }
            } else if (
                status == APDUReadResponseEnum.P1_LENGTH_GREATER_THAN_EF || 
                status == APDUReadResponseEnum.P1_PLUS_LENGTH_GREATER_THAN_EF) {
                    Log.e(TAG, "Remaining Read bytes: $remainingBytes and SW1 was $status")
                    // TODO: Create read loop for remaining bytes
            }
            else {
                Log.e(TAG, "Unable to Read APDU ${apdu.name} sw1 was $status and sw2 was $remainingBytes")
                throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
            }
    }

    private fun loopOrRead(
        cardChannel: CardChannel, 
        apdu: ApduCommand,
        getCardVersion: Boolean = false
        ) {
            if (apdu.lengthMin > 255) {
                var offset = 0
                // TODO: Implement Logic
                while (offset < 1) {
                    read(cardChannel, apdu, false, offset)
                    offset++
                }
            } else {
                read(cardChannel, apdu, getCardVersion)
            }
    }

    private fun read(cardChannel: CardChannel, 
        apdu: ApduCommand, 
        getCardVersion: Boolean = false,
        offset: Int = 0
        ) {
            try {
                var p1: String = String.format("%02d", offset)
                var readCommand = "00 B0 ${p1} 00 ${hexToBytesHelper.byteLength(apdu)}"

                Log.e(TAG, "Reading APDU ${apdu.name}, getCardVersion value: ${getCardVersion}, command: ${readCommand}")

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
                        aPDUResponseHelper.readResponseIntToAPDUReadResponse(sw1),
                        apdu,
                        cardChannel,
                        getCardVersion,
                        sw2!!,
                    )
                }
            } catch (e: CardException) {
                Log.e(TAG, "Unable to read card")
                throw Exception(UNABLE_TO_TRANSMIT_APDU_EXCEPTION)
            }
    }

    private fun getCardVersion(cardChannel: CardChannel, testGen1: Boolean = false) {
        try {
            val apduList: List<ApduCommand> = if (testGen1) apduCommandListGenerator.cardVersionCommandList() else apduCommandListGenerator.cardVersionGen2CommandList()
            select(cardChannel, apduList, true)
        }
        catch(e: Exception) {
            Log.e(TAG, "Unable to read card: ${e.message}")
            if (e.message == UNABLE_TO_TRANSMIT_APDU_EXCEPTION) {
                getCardVersion(cardChannel, true)
            }
            else {
                e.printStackTrace()
                throw e
            }
        }
    }

    private fun setCardStructureVersionAndNoOfVariables(hexString: String) {
        val hexValues = hexString.split(" ") // Split the hex string into individual byte values
        if (hexValues.size >= 17) {
            val generationHex = hexValues[1]
            val versionHex = hexValues[2] 
            
            val noOfEventsPerTypeHex = hexValues[3]
            val noOfFaultsPerTypeHex = hexValues[4]
            val noOfCardVehicleRecordsHex = hexValues[7] + hexValues[8]
            val noOfCardPlaceRecordsHex = hexValues[9] + hexValues[10]

            Log.e(TAG, "Card Structure Hex is: $hexString")
            Log.e(TAG, "Card Structure Card Generation is: $generationHex")
            Log.e(TAG, "Card Structure Card version number is: $versionHex")
            Log.e(TAG, "Card Structure noOfEventsPerTypeHex is: $noOfEventsPerTypeHex")
            Log.e(TAG, "Card Structure noOfFaultsPerTypeHex is: $noOfFaultsPerTypeHex")
            Log.e(TAG, "Card Structure noOfCardVehicleRecordsHex is: $noOfCardVehicleRecordsHex")
            Log.e(TAG, "Card Structure noOfCardPlaceRecordsHex is: $noOfCardPlaceRecordsHex")

            setCardGenerationAndVersion(generationHex, versionHex)

            noOfVarModel.noOfEventsPerType = noOfEventsPerTypeHex.toInt(16)
            noOfVarModel.noOfFaultsPerType = noOfFaultsPerTypeHex.toInt(16)
            noOfVarModel.noOfCardVehicleRecords = noOfCardVehicleRecordsHex.toInt(16)
            noOfVarModel.noOfCardPlaceRecords = noOfCardPlaceRecordsHex.toInt(16)

            Log.e(TAG, "Card Structure noOfEventsPerType is: ${noOfVarModel.noOfEventsPerType}")
            Log.e(TAG, "Card Structure noOfFaultsPerType is: ${noOfVarModel.noOfFaultsPerType}")
            Log.e(TAG, "Card Structure noOfCardVehicleRecords is: ${noOfVarModel.noOfCardVehicleRecords}")
            Log.e(TAG, "Card Structure noOfCardPlaceRecords is: ${noOfVarModel.noOfCardPlaceRecords}")

        } else if (hexValues.size >= 3) {
            val generationHex = hexValues[1]
            val versionHex = hexValues[2] 

            Log.e(TAG, "Card Structure Hex is: $hexString")
            Log.e(TAG, "Card Structure Card Generation is: $generationHex")
            Log.e(TAG, "Card Structure Card version number is: $versionHex")

            setCardGenerationAndVersion(generationHex, versionHex)
        }
        else {
            Log.e(TAG, "Hex string does not contain enough bytes.")
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

    private fun addToC1BFile(hexString: String, apdu: ApduCommand) {
        Log.e(TAG, "Hex string for ${apdu.name} was ${hexString}")
        currentReadStepStatusNotifier.updateState(uploadSteps++, channel)
        // TODO: Add to file
    }

    private fun performHashCommand(cardChannel: CardChannel) {
        val HASH_COMMAND: String = "80 2A 90 00"

        try {
            Log.e(TAG, "Performing Hash command ${HASH_COMMAND}")

            val commandAPDU = CommandAPDU(
                hexToBytesHelper.hexStringToByteArray(HASH_COMMAND)
            )
            
            Log.e(TAG, "Perform hash command")

            val response: ResponseAPDU = cardChannel.transmit(commandAPDU)

            val sw1: Int? = response.getSW1() // Get the SW1 part of the status word.
            val sw2: Int? = response.getSW2() // Get the SW2 part of the status word.

            if (sw1 != null && sw2 != null && sw1 == 0x90 && sw2 == 0x00) {
                // The response indicates success (SW1 = 0x90, SW2 = 0x00).
                // Process responseData accordingly.
                Log.e(TAG, "Hash command sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
            } else if (sw1 != null && sw1 == 0x6C) {
                val remainingBytes = sw2
                Log.e(TAG, "Remaining Read bytes: $remainingBytes")
            }
            else {
                // An error occurred. Handle the error based on the SW1 and SW2 values.
                Log.e(TAG, "Unable to make hash command sw1 was ${Integer.toHexString(sw1!!)} and sw2 was ${Integer.toHexString(sw2!!)}")
                // TODO: Handle the APDU error
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Unable to perform Hash command on card")
            e.printStackTrace()
            throw e
        }
    }

    private fun performSign() {
        val TG1_SIGNATURE = "00 2A 9E 9A 80" // 128 bytes
        var TG2_SIGNATURE: String? = null // 64…132 bytes
    }

    private fun disconnectCard(
        channel: MethodChannel,
        card: Card? = null
        ) {
        if (card != null) {
            card.disconnect(true)
        }
        uploadSteps = 0
        cardStructureVersion = null
        noOfVarModel = NoOfVarModel()
        deviceConnectionStatusNotifier.updateState("DISCONNECTED", channel)
        cardConnectionStateNotifier.updateState("DISCONNECTED", channel)
    }
}
