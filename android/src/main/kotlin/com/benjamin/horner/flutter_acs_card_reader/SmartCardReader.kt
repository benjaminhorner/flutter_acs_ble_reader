package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.SmartCardInitializer
import com.benjamin.horner.flutter_acs_card_reader.OnUpdateConnectionState
import com.benjamin.horner.flutter_acs_card_reader.CardConnectionStateNotifier
import com.benjamin.horner.flutter_acs_card_reader.FinalCardStatus
import com.benjamin.horner.flutter_acs_card_reader.CommonUtils
import com.benjamin.horner.flutter_acs_card_reader.MathUtils
import com.benjamin.horner.flutter_acs_card_reader.TGUtils
import com.benjamin.horner.flutter_acs_card_reader.StringUtils
import com.benjamin.horner.flutter_acs_card_reader.Driver
import com.sogestmatic.wrapper.Wrapper

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.content.Context
import android.app.Activity
import android.bluetooth.BluetoothManager
import io.flutter.plugin.common.MethodChannel
import java.io.UnsupportedEncodingException
import java.util.TimeZone
import java.util.GregorianCalendar
import java.util.Date

// ACS
import com.acs.smartcard.Reader
import com.acs.bluetooth.BluetoothReader
import com.acs.bluetooth.BluetoothReaderManager
import com.acs.bluetooth.BluetoothReaderGattCallback
import com.acs.bluetooth.Acr1255uj1Reader
import com.acs.bluetooth.Acr3901us1Reader

private val smartCardInitializer = SmartCardInitializer()

class SmartCardReader
    (val channel: MethodChannel) {
    private lateinit var device: BluetoothDevice
    private lateinit var activity: Activity
    private lateinit var driver: Driver
    private var sReponse520: String = ""
    private var mBluetoothReader: BluetoothReader? = null
    private var mBluetoothReaderManager: BluetoothReaderManager? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattCallback: BluetoothReaderGattCallback? = null
    private var bluetoothCardReadEnd: Boolean = false
    private var bluetoothReaderIsPowered: Boolean = false
    private var needsAuthentication: Boolean = false
    private var isReadingBluetoothCard: Boolean = false
    private var bluetoothCardReadIsFinished: Boolean = false
    private var authenticationSuccess: Boolean = false
    private var mCanStart: Boolean = false
    private var bluetoothCardReadHasEnded: Boolean = false
    private var isCardInserted: Boolean = false
    private var isFinishedReadingBluetoothCard: Boolean = false
    private var isAuthenticated: Boolean = false
    private var bluetoothIsReadingCard: Boolean = false
    private var onUpdateConnectionState: OnUpdateConnectionState? = null
    private var cardConnectionStateNotifier: CardConnectionStateNotifier = CardConnectionStateNotifier()
    private var mConnectState = BluetoothReader.STATE_DISCONNECTED
    private var FINAL_MASTER_KEY: String? = null
    private var FINAL_APDU_COMMAND: String? = null
    private var FINAL_ESCAPE_COMMAND: String? = null
    private var finalCardStatus: FinalCardStatus = FinalCardStatus.Unknown  // Card Final State: Present,Powered,Absent,PowerSaving
    private var finalATR: String? = null // Byte [] ATR Values
    private var oldState: Int = -1
    private var nSelect: Int = 0
    private var listSelect: ArrayList<Wrapper>? = null
    private var sResponse: String = ""

    private val TAG: String = "TAG"

    /* Default master key. */
    private val DEFAULT_3901_MASTER_KEY: String = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF"
    /* Get 8 bytes random number APDU. */
    private val DEFAULT_3901_APDU_COMMAND: String = "80 84 00 00 08"
    /* Get Serial Number command (0x02) escape command. */
    private val DEFAULT_3901_ESCAPE_COMMAND: String = "02"
    /* Default master key. */
    private val DEFAULT_1255_MASTER_KEY: String = "ACR1255U-J1 Auth"
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private val DEFAULT_1255_APDU_COMMAND: String = "FF B0 00 04 01"
    /* Get firmware version escape command. */
    private val DEFAULT_1255_ESCAPE_COMMAND: String = "E0 00 00 18 00"

    // APDU SELECT
    private val MF: String = "00 A4 02 0C 02 3F 00"
    private val EF_ICC: String = "00 A4 02 0C 02 00 02"
    private val EF_IC: String = "00 A4 02 0C 02 00 05"
    private val DF_TACHOGRAPH: String = "00 A4 04 0C 06 FF 54 41 43 48 4F"
    private val EF_APP_IDENTIFICATION: String = "00 A4 02 0C 02 05 01"
    private val EF_IDENTIFICATION: String = "00 A4 02 0C 02 05 20"
    private val EF_CARD_DOWNLOAD: String = "00 A4 02 0C 02 05 0E"
    private val EF_DRIVING_LICENCE_INFO: String = "00 A4 02 0C 02 05 21"
    private val EF_EVENTS_DATA: String = "00 A4 02 0C 02 05 02"
    private val EF_FAULTS_DATA: String = "00 A4 02 0C 02 05 03"
    private val EF_DRIVER_ACTIVITY_DATA: String = "00 A4 02 0C 02 05 04"
    private val EF_VEHICULES_USED: String = "00 A4 02 0C 02 05 05"
    private val EF_PLACES: String = "00 A4 02 0C 02 05 06"
    private val EF_CURRENT_USAGE: String = "00 A4 02 0C 02 05 07"
    private val EF_CONTROL_ACTIVITY_DATA: String = "00 A4 02 0C 02 05 08"
    private val EF_SPECIFIC_CONDITIONS: String = "00 A4 02 0C 02 05 22"
    private val EF_CARD_CERTIFICATE: String = "00 A4 02 0C 02 C1 00"
    private val EF_CA_CERTIFICATE: String = "00 A4 02 0C 02 C1 08"
    private val DF_TACHOGRAPH_G2: String = "00 A4 04 0C 06 FF 53 4D 52 44 54"
    private val EF_VEHICULEUNITS_USED: String = "00 A4 02 0C 02 05 23"
    private val EF_GNSS_PLACES: String = "00 A4 02 0C 02 05 24"
    private val EF_CARDSIGNCERTIFICATE: String = "00 A4 02 0C 02 C1 01"
    private val EF_LINK_CERTIFICATE: String = "00 A4 02 0C 02 C1 09"

    // Tachygraphs 1 & 2
    private var wTG2 = Wrapper(DF_TACHOGRAPH_G2, 0, "Tachograph G2", false, true, false)
    private var wMF = Wrapper(MF, 0, "MF", false, false, false)
    private var wTG1 = Wrapper(DF_TACHOGRAPH, 0, "Tachograph G1", false, false, false)
    private var wICC = Wrapper(EF_ICC, 25, "ICC", false, false, true)
    private var wIC = Wrapper(EF_IC, 8, "IC", false, false, true)
    private var wTG1_APP = Wrapper(EF_APP_IDENTIFICATION, 10, "App Identification", true, false, true)
    private var wTG2_APP = Wrapper(EF_APP_IDENTIFICATION, 17, "App Identification", true, true, true)
    private var wTG1_ID = Wrapper(EF_IDENTIFICATION, 143, "Identification", true, false, true)
    private var wTG2_ID = Wrapper(EF_IDENTIFICATION, 143, "Identification", true, true, true)
    private var wTG1_CARDDWL = Wrapper(EF_CARD_DOWNLOAD, 4, "Card download", true, false, true)
    private var wTG2_CARDDWL = Wrapper(EF_CARD_DOWNLOAD, 4, "Card download", true, true, true)
    private var wTG1_DRIVING = Wrapper(EF_DRIVING_LICENCE_INFO, 53, "Driving licence", true, false, true)
    private var wTG2_DRIVING = Wrapper(EF_DRIVING_LICENCE_INFO, 53, "Driving licence", true, true, true)
    private var wTG1_EVENTS = Wrapper(EF_EVENTS_DATA, 0, "Events data", true, false, true)
    private var wTG2_EVENTS = Wrapper(EF_EVENTS_DATA, 0, "Events data", true, true, true)
    private var wTG1_FAULTS = Wrapper(EF_FAULTS_DATA, 0, "Faults data", true, false, true)
    private var wTG2_FAULTS = Wrapper(EF_FAULTS_DATA, 0, "Faults data", true, true, true)
    private var wTG1_DRIVER = Wrapper(EF_DRIVER_ACTIVITY_DATA, 0, "Driver activity data", true, false, true)
    private var wTG2_DRIVER = Wrapper(EF_DRIVER_ACTIVITY_DATA, 0, "Driver activity data", true, true, true)
    private var wTG1_VUSED = Wrapper(EF_VEHICULES_USED, 0, "Vehicules used", true, false, true)
    private var wTG2_VUSED = Wrapper(EF_VEHICULES_USED, 0, "Vehicules used", true, true, true)
    private var wTG1_PLACES = Wrapper(EF_PLACES, 0, "Places", true, false, true)
    private var wTG2_PLACES = Wrapper(EF_PLACES, 0, "Places", true, true, true)
    private var wTG1_CURRENT = Wrapper(EF_CURRENT_USAGE, 19, "Current usage", true, false, true)
    private var wTG2_CURRENT = Wrapper(EF_CURRENT_USAGE, 19, "Current usage", true, true, true)
    private var wTG1_CONTROL = Wrapper(EF_CONTROL_ACTIVITY_DATA, 46, "Control activity data", true, false, true)
    private var wTG2_CONTROL = Wrapper(EF_CONTROL_ACTIVITY_DATA, 46, "Control activity data", true, true, true)
    private var wTG1_SPECIFIC = Wrapper(EF_SPECIFIC_CONDITIONS, 280, "Specific conditions", true, false, true)
    private var wTG2_SPECIFIC = Wrapper(EF_SPECIFIC_CONDITIONS, 0, "Specific conditions", true, true, true)
    private var wTG2_VUNITS = Wrapper(EF_VEHICULEUNITS_USED, 0, "Vehicule units used", true, true, true)
    private var wTG2_GNSS = Wrapper(EF_GNSS_PLACES, 0, "GNSS places", true, true, true)
    private var wTG1_CARDCERTIF = Wrapper(EF_CARD_CERTIFICATE, 194, "Card certificate", false, false, true)
    private var wTG2_CARDCERTIF = Wrapper(EF_CARD_CERTIFICATE, 0, "Card certificate", false, true, true)
    private var wTG1_CACERTIF = Wrapper(EF_CA_CERTIFICATE, 194, "CA certificate", false, false, true)
    private var wTG2_CACERTIF = Wrapper(EF_CA_CERTIFICATE, 0, "CA certificate", false, true, true)
    private var wTG2_CARDSIGNCERTIF = Wrapper(EF_CARDSIGNCERTIFICATE, 0, "Card sign certificate", false, true, true)
    private var wTG2_LINKCERTIF = Wrapper(EF_LINK_CERTIFICATE, 0, "Link certificate", false, true, true)
    private var IS_TG2: Boolean = false
    private var TIMESTAMP: Int = 0

    fun readSmartCard(device: BluetoothDevice, activity: Activity, context: Context, driver: Driver): String {
        mGattCallback = BluetoothReaderGattCallback()
        mBluetoothReaderManager = BluetoothReaderManager()
        this.device = device
        this.activity = activity
        this.driver = driver

        mGattCallback!!.setOnConnectionStateChangeListener { gatt, state, newState ->
            try {
                    //onUpdateConnectionState?.updateState(state, gatt.connect(), newState)

                    if (state != BluetoothGatt.GATT_SUCCESS) {
                        mConnectState = BluetoothReader.STATE_DISCONNECTED
                        if (newState == BluetoothReader.STATE_CONNECTED) {
                            Log.e("Gatt Callback", "newState is BluetoothReader.STATE_CONNECTED")
                        } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                            Log.e("Gatt Callback", "newState is BluetoothReader.STATE_DISCONNECTED")
                            cardConnectionStateNotifier.updateState(newState, channel)
                        }
                        reset()
                        isReadingBluetoothCard = false
                        isReadingBluetoothCard = false
                        isCardInserted = false
                        bluetoothReaderIsPowered = false
                        isFinishedReadingBluetoothCard = true
                    }
                    cardConnectionStateNotifier.updateState(newState, channel)
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        if (mBluetoothReaderManager != null) {
                            mBluetoothReaderManager?.detectReader(
                                gatt, mGattCallback
                            )
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mBluetoothReader = null
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt!!.close()
                            mBluetoothGatt = null
                        }
                        cardConnectionStateNotifier.updateState(newState, channel)

                    }
                } catch (e: Exception) {
                    Log.e("GATT_ERROR", e.message.toString())
                    throw e
                }
        }

        mBluetoothReaderManager?.setOnReaderDetectionListener { reader ->
            smartCardInitializer.initCardReader(reader)
            when (reader) {
                is Acr3901us1Reader -> {
                    Log.e("DeviceFound", "Used + ACR3901U-S1Reader")
                }
                is Acr1255uj1Reader -> {
                    Log.e("DeviceFound", "Used + Acr1255uj1Reader")
                }
                else -> {
                    disconnectReader()
                }
            }
            if (!isFinishedReadingBluetoothCard) {
                mBluetoothReader = reader
                setListeners(activity)
                activateReader()
            }
        }

        connectReader(device, activity)

        // TODO: Return the actual card Data
        val data = "SmartCard data"
        return data
    }

    private fun activateReader() {
        if (mBluetoothReader == null) {
            Log.e("activateReader", "mBluetoothReader is NULL")
            return
        }
        if (mBluetoothReader is Acr3901us1Reader) {
            Log.e("mBluetoothReader", "Bonding Acr3901us1Reader")
            (mBluetoothReader as Acr3901us1Reader).startBonding()
        } else if (mBluetoothReader is Acr1255uj1Reader) {
            Log.e("mBluetoothReader", "Notification Acr1255uj1Reader")
            (mBluetoothReader as? Acr1255uj1Reader)?.enableNotification(true)
        }
    }

    private fun disconnectReader() {
        Log.e("disconnectReader", "disconecting Reader")
        if (mBluetoothGatt == null) {
            Log.e("disconnectReader", "mBluetoothGatt is NULL")
            bluetoothIsReadingCard = false
            isReadingBluetoothCard = false
            return
        }
        Log.e("disconnectReader", "mBluetoothGatt disconnect")
        mBluetoothGatt?.disconnect()
        bluetoothIsReadingCard = false
        isReadingBluetoothCard = false
    }

    private fun connectReader(device: BluetoothDevice, activity: Activity) {
        Log.e("connectReader", "connecting to Reader")

        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (bluetoothManager == null) {
            Log.e("connectReader", "bluetoothManager is NULL")
            return
        }
        val bluetoothAdapter = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            Log.e("connectReader", "bluetoothAdapter is NULL")
            return
        }
        
        if (mBluetoothGatt != null) {
            Log.e("mBluetoothGatt", "Clear old GATT connection")
            mBluetoothGatt?.disconnect()
            mBluetoothGatt?.close()
            mBluetoothGatt = null
        }
        val device = bluetoothAdapter.getRemoteDevice(device.address)
        if (device == null) {
            Log.w("device", "Device not found. Unable to connect.")
            return
        }
        Log.e("connectReader", "Start GAT conect")
        mBluetoothGatt = device.connectGatt(activity.applicationContext, false, mGattCallback)
    }

    private fun reset() {
        Log.e("reset", "reset all keys & commands")
        FINAL_MASTER_KEY = null
        FINAL_APDU_COMMAND = null
        FINAL_ESCAPE_COMMAND = null
        finalCardStatus = FinalCardStatus.Unknown
        finalATR = null
    }

    private fun powerOnCard() {
        Log.e("powerOnCard", "Trying to power on the card")
        if (mBluetoothReader == null) {
            Log.e("powerOnCard", "mBluetoothReader is NULL")
            //onUpdateReaderView?.onUpadateMAJUI("LIB_Messages", "gsCardNotReady", 1)
            return
        }
        if (!mBluetoothReader!!.powerOnCard()) {
            Log.e("powerOnCard", "Card powered on!")
            //onUpdateReaderView?.onUpadateMAJUI("LIB_Messages", "gsCardNotReady", 1)
        }
        if (!mBluetoothReader!!.cardStatus) {
            Log.e("powerOnCard", "Card status is" + mBluetoothReader!!.cardStatus)
            //onUpdateReaderView?.onUpadateMessages(
            //    "LIB_Messages",
            //    "cardStatus" + mBluetoothReader!!.cardStatus, 0
            //)
            //mTxtSlotStatus.setText(R.string.card_reader_not_ready)
        }
    }

    private fun powerOffCard() {
        if (mBluetoothReader == null) {
            //	mTxtATR.setText(R.string.card_reader_not_ready);
            return
        }
        if (!mBluetoothReader?.powerOffCard()!!) {
            //mTxtATR.setText(R.string.card_reader_not_ready);
        }
    }

    private fun authenticate(): Boolean {
        Log.e("authenticate", "authenticating…")
        isAuthenticated = false

        val masterKey = CommonUtils.getEditTextinHexBytes(FINAL_MASTER_KEY!!)

        Log.e("masterKey", "" + masterKey.toString())
        if (masterKey != null && masterKey.isNotEmpty()) {

            if (mBluetoothReader != null && mBluetoothReader?.authenticate(masterKey)!!) {
                Log.e("authenticate", "Tryig to Autheticate user")
                //onUpdateReaderView?.onUpadateMAJUI("LIB_Jumelage", "gsAuthentificationEncours", 1)
            }

        }
        return isAuthenticated
    }

    private fun initCardReader(reader: BluetoothReader) {
        if (reader is Acr3901us1Reader) {
            /* The connected reader is ACR3901U-S1 reader. */
            if (FINAL_MASTER_KEY == null) {
                FINAL_MASTER_KEY = DEFAULT_3901_MASTER_KEY
            }
            if (FINAL_APDU_COMMAND == null) {
                FINAL_APDU_COMMAND = DEFAULT_3901_APDU_COMMAND
            }
            if (FINAL_ESCAPE_COMMAND == null) {
                FINAL_ESCAPE_COMMAND = DEFAULT_3901_ESCAPE_COMMAND
            }
        } else if (reader is Acr1255uj1Reader) {
            /* The connected reader is ACR1255U-J1 reader. */
            if (FINAL_MASTER_KEY?.length == 0) {
                try {
                    val charset = Charsets.UTF_8
                    FINAL_MASTER_KEY = toHexString(
                        DEFAULT_1255_MASTER_KEY
                            .toByteArray(charset)
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
            if (FINAL_APDU_COMMAND?.length == 0) {
                FINAL_APDU_COMMAND = DEFAULT_1255_APDU_COMMAND
            }
            if (FINAL_ESCAPE_COMMAND?.length == 0) {
                FINAL_ESCAPE_COMMAND = DEFAULT_1255_ESCAPE_COMMAND
            }
        }
        Log.e(TAG, "FINAL_MASTER_KEY: $FINAL_MASTER_KEY")
    }

    private fun getCardStatus(cardStatus: Int) {
        when (cardStatus) {
            BluetoothReader.CARD_STATUS_ABSENT -> {

                Log.e("TAG", "CARD_STATUS_ABSENT")
                //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatut", " gsCARD_STATUS_ABSENT", 1)
                // TODO: Envoyer au code Dart que la carte est ABSENTE
                finalCardStatus = FinalCardStatus.Absent
                finalATR = ""
                if (isReadingBluetoothCard) {
                    //onUpdateReaderView?.onchangeViewTo(7)
                } else {
                    //onUpdateReaderView?.onchangeViewTo(7)
                }
                isCardInserted = false
                //bluetoothReaderIsPowered = false
                bluetoothIsReadingCard = false
                isReadingBluetoothCard = false
                //onUpdateReaderView?.onUpadateMessages("RecupererAction", "false gbSEnLecture", 0)
                //onUpdateReaderView?.onUpadateMessages(
                //    "RecupererAction",
                //    "true gbTraitementTerminer", 0
                //)


            }
            BluetoothReader.CARD_STATUS_PRESENT -> {
                if (isFinishedReadingBluetoothCard) {
                    Log.e("TAG", "" + isReadingBluetoothCard + "" + isFinishedReadingBluetoothCard)
                    return
                }
                Log.e("TAG", "CARD_STATUS_PRESENT")
                mBluetoothReader?.let { initCardReader(it) }
                activateReader()
                // TODO: Notify Update to the Dart code 
                //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatut", " gsCARD_STATUS_PRESENT", 1)
                //onUpdateReaderView?.onchangeViewTo(8)
                finalCardStatus = FinalCardStatus.Present
                finalATR = ""
                MathUtils.sHexa = ""

                if (!isAuthenticated) {
                    return
                }
                if (bluetoothReaderIsPowered) {
                    bluetoothReaderIsPowered = true
                    return
                }
                powerOnCard()
                if (!mBluetoothReader?.powerOnCard()!!) {
                    Log.e("TAG", "CARD_STATUS_PRESENT 2")
                    //onUpdateReaderView?.onUpadateMAJUI("LIB_Messages", " gsCardNotReady", 1)
                    bluetoothReaderIsPowered = false
                } else {
                    Log.e("TAG", "CARD_STATUS_PRESENT 3")
                    bluetoothReaderIsPowered = true
                }
                if (!mBluetoothReader?.getCardStatus()!!) {
                    Log.e("TAG", "CARD_STATUS_PRESENT 4")
                    //onUpdateReaderView?.onUpadateMAJUI("LIB_Messages", " gsCardNotReady", 1)
                }

            }
            BluetoothReader.CARD_STATUS_POWERED -> {
                //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatut", " gsCARD_STATUS_POWERED", 1)
                finalCardStatus = FinalCardStatus.Active
                isCardInserted = true
                bluetoothReaderIsPowered = true
                Log.e("TAG", "CARD_STATUS_POWERED")
            }
            BluetoothReader.CARD_STATUS_POWER_SAVING_MODE -> {
                Log.e("TAG", "CARD_STATUS_POWER_SAVING_MODE")
                //onUpdateReaderView?.onUpadateMAJUI(
                //    "LIB_CardStatut",
                //    " gsCARD_STATUS_POWER_SAVING_MODE",
                //    1
                //)
                finalCardStatus = FinalCardStatus.Standby
                //    onUpdateReaderView?.onchangeViewTo(7)

                isCardInserted = false
                bluetoothReaderIsPowered = false
                bluetoothIsReadingCard = false
                isReadingBluetoothCard = false
                // TODO: Notify Dart code
                //onUpdateReaderView?.onUpadateMessages(
                //    "onUpdateReaderView",
                //    "gbSEnLecture --> false", 0
                //)
                //onUpdateReaderView?.onUpadateMessages(
                //    "onUpdateReaderView",
                //    "gbTraitementTerminer --> true", 0
                //)
            }
            else -> {
                Log.e("TAG", "Card status is unknown")
                //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatut", " gsCARD_STATUS_UKNOWN", 1)
                finalCardStatus = FinalCardStatus.Unknown
                //onUpdateReaderView?.onchangeViewTo(3)
                isCardInserted = false
                bluetoothReaderIsPowered = false
                bluetoothIsReadingCard = false
                isReadingBluetoothCard = false
                // TODO: Notify Dart code
                //onUpdateReaderView?.onUpadateMessages(
                //    "onUpdateReaderView",
                //    "gbSEnLecture --> false", 0
                //)
                //onUpdateReaderView?.onUpadateMessages(
                //    "onUpdateReaderView",
                //    "gbTraitementTerminer --> true", 0
                //)
            }
        }
    }
    

    private fun setListCommandSelect() {
        listSelect = ArrayList()

        listSelect?.let { list ->
            list.add(wTG2)
            list.add(wMF)
            list.add(wICC)
            list.add(wIC)
            list.addAll(setListeTG1())
        }
    }

    private fun setListeTG1(): java.util.ArrayList<Wrapper> {
        val list = java.util.ArrayList<Wrapper>()
        list.add(wTG1)
        list.add(wTG1_CARDCERTIF)
        list.add(wTG1_CACERTIF)
        list.add(wTG1_APP)
        list.add(wTG1_ID)
        list.add(wTG1_CARDDWL)
        list.add(wTG1_DRIVING)
        list.add(wTG1_EVENTS)
        list.add(wTG1_FAULTS)
        list.add(wTG1_DRIVER)
        list.add(wTG1_VUSED)
        list.add(wTG1_PLACES)
        list.add(wTG1_CURRENT)
        list.add(wTG1_CONTROL)
        list.add(wTG1_SPECIFIC)
        list.add(wTG1)
        list.add(wMF)
        return list
    }

    /*
    Liste des commandes TG2 Tachograph G2
    */
    private fun setListeTG2(): java.util.ArrayList<Wrapper> {
        val list = java.util.ArrayList<Wrapper>()
        list.add(wTG2)
        list.add(wTG2_CARDCERTIF)
        list.add(wTG2_CACERTIF)
        list.add(wTG2_CARDSIGNCERTIF)
        list.add(wTG2_LINKCERTIF)
        list.add(wTG2_APP)
        list.add(wTG2_ID)
        list.add(wTG2_CARDDWL)
        list.add(wTG2_DRIVING)
        list.add(wTG2_EVENTS)
        list.add(wTG2_FAULTS)
        list.add(wTG2_DRIVER)
        list.add(wTG2_VUSED)
        list.add(wTG2_PLACES)
        list.add(wTG2_CURRENT)
        list.add(wTG2_CONTROL)
        list.add(wTG2_SPECIFIC)
        list.add(wTG2_VUNITS)
        list.add(wTG2_GNSS)
        list.add(wTG2)
        list.add(wMF)
        return list
    }

    fun getErrorToString(errorCode: Int): String {
        // TODO: return errors to Dart
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_SUCCESS", 1)
            return ""
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_INVALID_CHECKSUM",
            //    1
            //)
            return "The checksum is invalid."
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_INVALID_DATA_LENGTH",
            //    1
            //)
            return "The data length is invalid."
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_INVALID_COMMAND",
            //    1
            //)
            return "The command is invalid."
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_UNKNOWN_COMMAND_ID",
            //    1
            //)
            return "The command ID is unknown."
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_CARD_OPERATION",
            //    1
            //)
            return "The card operation failed."
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_AUTHENTICATION_REQUIRED",
            //    1
            //)
            return "Authentication is required."
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_LOW_BATTERY", 1)
            return "The battery is low."
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_CHARACTERISTIC_NOT_FOUND",
            //    1
            //)
            return "Error characteristic is not found."
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_WRITE_DATA", 1)
            return "Write command to reader is failed."
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_TIMEOUT", 1)
            return "Timeout."
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_AUTHENTICATION_FAILED",
            //    1
            //)
            return "Authentication is failed."
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_UNDEFINED", 1)
            return "Undefined error."
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_INVALID_DATA", 1)
            return "Received data error."
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            //onUpdateReaderView?.onUpadateMAJUI(
            //    "LIB_CardStatutAvailable",
            //    "gsERROR_COMMAND_FAILED",
            //    1
            //)
            return "The command failed."
        } else {
            //onUpdateReaderView?.onUpadateMAJUI("LIB_CardStatutAvailable", "gsERROR_UNKNOWN", 1)
            return "Unknown error."
        }

    }

    private fun getAPDUResponseString(response: ByteArray, errorCode: Int): String {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null) {
                return toHexString(response)
            }
            return toHexString(response)
        }
        return getErrorToString(errorCode)
    }

    private fun toHexString(buffer: ByteArray): String {
        var bufferString = ""
        for (i in buffer.indices) {
            var hexChar = Integer.toHexString(buffer[i].toInt() and 0xFF)
            if (hexChar.length == 1) {
                hexChar = "0$hexChar"
            }

            bufferString += hexChar.toUpperCase() + " "
        }
        return bufferString
    }

    private fun sendAPDUCommandToTransmit(commande: String) {
        if (!isFinishedReadingBluetoothCard) {
            Log.e("sendAPDUCommandToTransmit", "--> " + commande)
            transmitApdu(commande)
        }

    }

    private fun transmitApdu(commande: String) {
        if (mBluetoothReader == null) {
            finalCardStatus = FinalCardStatus.NotReady
            return
        }
        if (finalCardStatus != FinalCardStatus.Active) {
            bluetoothCardReadEnd = false
            powerOffCard()
            if (mBluetoothGatt != null) {
                mBluetoothGatt?.disconnect()
                mBluetoothGatt?.close()
                mBluetoothGatt = null
            }
            connectReader(device, activity)
        }

        /* Retrieve APDU command from edit box. */
        val apduCommand = CommonUtils.getEditTextinHexBytes(commande)

        if (apduCommand != null && apduCommand.size > 0) {

            /* Transmit APDU command. */
            Log.e("cmd", "" + commande)
            if (!mBluetoothReader?.transmitApdu(apduCommand)!!) {
                finalCardStatus = FinalCardStatus.NotReady
            }

        } else {
            //appelProcedureWL("InfoWL","Format Error");
        }
    }

    fun createC1BFile() {
        //var tachyphone = "tachyphone2017"
        //var tachyphoneMd5 = MD5Utils.encryptStr(tachyphone)

        //val sHexaEncrypted = AESUtils.encrypt(MathUtils.sHexa, tachyphoneMd5)
        //Log.e("shexaaaaaaaaaa", "MathUtils.sHexa = " + MathUtils.sHexa)
        //val sHexaDecryption = AESUtils.decrypt(sHexaEncrypted, tachyphoneMd5)

        //var sTemp = StringUtils.getHexString(sReponse520, 1)
        //val sPays = TACHO_NATION_TABLE(sTemp)
        //Log.d("sPays::",""+sPays)
        //sTemp = sReponse520.substring(3)

        ////07/05/2019 carte conducteur sur 14 caractères (les 2 derniers sont susceptibles d'être modifier)
        ////sTemp = StringUtils.getHexString(sTemp, 16);
        //sTemp = StringUtils.getHexString(sTemp, 14)
        //sTemp = sTemp.replace(" ", "")
        //val sCardNum = StringUtils..convertHexToString(sTemp)

        //val aujourdhui = Date()
        //val formater = SimpleDateFormat("yyMMddHHmm", Locale.getDefault())
        //val sDate = formater.format(aujourdhui)
        //val sAcsCardRenamed = sPays + sCardNum.substring(0, 14) + sDate
        //val pathPackage = activity.applicationInfo.dataDir

        ////Toast.makeText(getApplicationContext(), pathPackage, Toast.LENGTH_LONG).show();
        //val sXML = "$pathPackage/$sAcsCardRenamed.xml"
        ////Creation xml

        //setPathXML(sXML)
        ////appelProcedureWL("changePlan",16);
        ////[CA]05/08/2019 pour les tests pas d'envoi
        //val user = Hawk.get("USER_DATA") as Driver
        //val agence = Hawk.get<String>("ID_AGENCE")

        //val create_xml =
        //    XmlUtils.createXML(
        //        AESUtils.encrypt(agence, tachyphoneMd5),
        //        AESUtils.encrypt("vrai", tachyphoneMd5),
        //        user,
        //        Card(),
        //        Cost(),
        //        sHexaEncrypted
        //    )
        //onUploadFile?.onUploadFile(create_xml, sAcsCardRenamed)
        //// disconnectReader()
        //// mBluetoothReader?.powerOffCard()
        ////  mBluetoothGatt?.disconnect()
        ////  clicEnvoyerXml();

    }

    private fun calculateEFSize(app: Wrapper, apduResponse: String) {
        val n1: Int
        val n2: Int
        val n3: Int
        val n4: Int
        val n6: Int
        val n7: Int
        val n8: Int
        var n9 = 0
        val nEvents: Int
        val nFaults: Int
        val nVUsed: Int
        val nPlaces: Int
        val nActivity: Int
        val nVUnits: Int
        val nGNSS: Int
        var nSpecific = 0
        /*Récupération des valeurs relevé dans la carte commun*/
        n1 = MathUtils.hexaToDecimal(apduResponse.substring(9, 11)) //Events
        n2 = MathUtils.hexaToDecimal(apduResponse.substring(12, 14)) //Faults
        n6 = MathUtils.hexaToDecimal(apduResponse.substring(15, 20)) //Activity
        n3 = MathUtils.hexaToDecimal(apduResponse.substring(21, 26)) //Vehicles used
        /*Calcul de la taille des EF commun*/
        nFaults = n2 * 24 * 2
        nActivity = n6 + 4
        if (!app.getTG2()) { //DF Tachograph G1
            /*Récupération des valeurs relevé dans la carte*/
            n4 = MathUtils.hexaToDecimal(apduResponse.substring(27, 29)) //Places
            /*Calcul de la taille des EF*/
            nEvents = n1 * 24 * 6
            nVUsed = n3 * 31 + 2
            nPlaces = n4 * 10 + 1
            /*Mise a jour de la taille des objets Wrapper*/
            wTG1_EVENTS.taille = nEvents
            wTG1_FAULTS.taille = nFaults
            wTG1_DRIVER.taille = nActivity
            wTG1_VUSED.taille = nVUsed
            wTG1_PLACES.taille = nPlaces
        } else { // DF Tachograph G2
            n4 = MathUtils.hexaToDecimal(apduResponse.substring(27, 32)) // Places
            n8 = MathUtils.hexaToDecimal(apduResponse.substring(33, 38)) // GNSS
            n9 = MathUtils.hexaToDecimal(apduResponse.substring(39, 44)) // Specific condition
            n7 = MathUtils.hexaToDecimal(apduResponse.substring(45, 50)) // Vehicles unit
            /* Calcul de la taille des EF */
            nEvents = n1 * 24 * 11
            nVUsed = n3 * 48 + 2
            nVUnits = n7 * 10 + 2
            nPlaces = n4 * 21 + 2
            nGNSS = n8 * 18 + 2
            nSpecific = n9 * 5 + 2
            /* Mise a jour de la taille des objets Wrappe r*/
            wTG2_EVENTS.taille = nEvents
            wTG2_FAULTS.taille = nFaults
            wTG2_DRIVER.taille = nActivity
            wTG2_VUSED.taille = nVUsed
            wTG2_PLACES.taille = nPlaces
            wTG2_VUNITS.taille = nVUnits
            wTG2_GNSS.taille = nGNSS
            wTG2_SPECIFIC.taille = nSpecific
        }
    }

    private fun setUpdate(): String {
        var timesHexa: String = ""
        var tz = TimeZone.getDefault();
        var cal = GregorianCalendar.getInstance(tz);
        var  offsetInMillis = (tz.getOffset(cal.getTimeInMillis()) + Date().time)/1000

        if (TIMESTAMP == 0) {
            timesHexa = offsetInMillis.toString(16).toUpperCase()

        }
        return "00 D6 00 00 04 $timesHexa"
    }

    private fun setListeners(activity: Activity) {
        Log.e("setListeners", "settting listeners")
        val mContext = activity.applicationContext

        mBluetoothReader?.setOnAuthenticationCompleteListener { bluetoothReader, errorCode ->

            if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                isAuthenticated = true
                powerOnCard()
                authenticationSuccess = true
                Log.e("authenticate", "Success")
            } else {
                authenticate()
                Log.e("authenticate", "Fail : " + errorCode)
                if(!authenticationSuccess){
                    Log.e("authenticate", "Failed to authenticate (authenticationSuccess == false) : " + errorCode)
                }

                if(mBluetoothReader != null){
                    val escapeCommand = CommonUtils.getEditTextinHexBytes("30 00")
                    mBluetoothReader?.transmitEscapeCommand(escapeCommand)
                }
                isAuthenticated = false
            }
        }

        mBluetoothReader?.setOnCardStatusChangeListener { bluetoothReader, sta ->
            if (sta != oldState && !isAuthenticated) {
                getCardStatus(sta)
            }
            oldState = sta
        }

        mBluetoothReader?.setOnEnableNotificationCompleteListener { bluetoothReader, result ->
            if (result != BluetoothGatt.GATT_SUCCESS) {
                /* Fail */
                Log.e("setOnEnableNotificationCompleteListener", "The device is unable to set notification for reader!")
                // TODO: notify Dart code
            } else {
                /* Device is ready to be used: Authenticate the user */
                authenticate()
            }
        }

        mBluetoothReader?.setOnDeviceInfoAvailableListener { bluetoothReader, infoId, bytes, status ->
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("AvailableListener", "Info : " + infoId)
            }
        }

        /* Handle on slot status available. */
        mBluetoothReader?.setOnCardStatusAvailableListener { bluetoothReader, cardStatus, errorCode ->
            if (errorCode != BluetoothReader.ERROR_SUCCESS) {
                getErrorToString(errorCode)

                Log.e("AvailableListener", "Not : " + getErrorToString(errorCode))
            } else {
                Log.e("AvailableListener", "can : ")
                getCardStatus(cardStatus)
                authenticate()
            }
        }

        /* Wait for receiving ATR string. */
        mBluetoothReader?.setOnAtrAvailableListener { bluetoothReader, atr, errorCode ->

            if (atr == null) {
                    //mTxtATR.setText(getErrorString(errorCode))
                    finalATR = null
                    Log.e("AtrAvailableListener", "Not : " + getErrorToString(errorCode))
                    if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
                        //onUpdateReaderView?.onchangeViewTo(5)
                        //  TODO: Notify Dart code
                    }

                    bluetoothIsReadingCard = false
                    isReadingBluetoothCard = false

                } else {
                    powerOnCard()
                    finalATR = toHexString(atr)
                    bluetoothCardReadHasEnded = false
                    bluetoothIsReadingCard = true
                    isCardInserted = true
                    isReadingBluetoothCard = true
                    //onUpdateReaderView?.onchangeViewTo(8)
                    nSelect = 0
                    setListCommandSelect()

                    Log.e("cmd", "--> " + listSelect?.get(nSelect)?.commande)
                    mBluetoothReader?.transmitApdu(listSelect?.get(nSelect)?.commande?.let {
                        CommonUtils.getEditTextinHexBytes(it)
                    })

                }
        }

        mBluetoothReader?.setOnCardPowerOffCompleteListener { bluetoothReader, result ->
            Log.e("PowerOffListener", "Not : " + getErrorToString(result))
            getErrorToString(result)
        }

        // Variables for APDU LISTENER
        var readSize = 0
        var selectSize = 0
        var isRead = false
        var isCmdSignature = false
        var needsHeader = false
        var listRead = ArrayList<String>()
        var nRead = -1
        var sC1BContent = ""
        var libGauge = ""
        var TG2CertificateSize = 0
        var sReponseCardSignCertif = ""
        var end = false
        var nTG1 = 0
        var nTG2 = 0

        mBluetoothReader?.setOnResponseApduAvailableListener { bluetoothReader, apdu, errorCode ->

            activity.runOnUiThread {
                // Card status check
                if (finalCardStatus != FinalCardStatus.Active && mBluetoothReader == null) {
                    return@runOnUiThread
                }

                try {
                    Log.e("Commande", "" + listSelect?.get(nSelect)?.commande)
                    sResponse = getAPDUResponseString(apdu, errorCode)
                    isReadingBluetoothCard = true
                    val sReponseWithoutWhiteSpaces = sResponse.replace("\\s+".toRegex(), "")
                    val result = StringUtils.convertHexToString(sReponseWithoutWhiteSpaces)

                    if (!end) {
                        if (listSelect?.get(nSelect)?.nom?.contains("certificate")!! && listSelect?.get(
                                nSelect
                            )!!.tG2
                        ) {
                            if (nRead == 0) {
                                TG2CertificateSize = (sResponse.length / 3) - 2
                                if (sResponse.length >= 768) { // size > 256 (256*3char = 768char)
                                    isRead = true
                                    listRead.add("00 B0 00 FE 00")
                                } else {
                                    listSelect?.get(nSelect)!!.taille = TG2CertificateSize
                                }
                                if (listSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
                                    sReponseCardSignCertif = sResponse
                                }
                            } else if (nRead == 1) {
                                TG2CertificateSize += (sResponse.length / 3) - 2
                                listSelect?.get(nSelect)!!.taille = TG2CertificateSize
                                if (listSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
                                    sReponseCardSignCertif += " $sResponse"
                                }
                            }
                        }

                        if (nSelect == 0) {
                            if (sResponse.trim() == "90 00") {
                                IS_TG2 = true
                                listSelect?.addAll(setListeTG2())
                            } else {
                                IS_TG2 = false
                            }
                            selectSize = listSelect!!.size - 1
                            nSelect++
                            nRead = -1
                            nTG1 = 0
                            nTG2 = 0
                            TIMESTAMP = 0
                            isRead = false
                            sendAPDUCommandToTransmit(listSelect?.get(nSelect)!!.commande)

                            Log.e("ApduAvailable", "Comm1 : ${listSelect?.get(nSelect)?.commande}")
                        } else {
                            // Default
                            if (sResponse.trim().endsWith("90 00")) {
                                sC1BContent = sResponse.substring(
                                    0,
                                    sResponse.length - 6
                                ) // Delete "90 00" at the end of sResponse
                                if (sC1BContent.replace("\\s", "").isNotEmpty()) {
                                    /* La commande en cours est une signature */
                                    isCmdSignature = listRead[nRead].startsWith("00 2A 9E 9A")
                                    /* Commad is runnig and needs a header */
                                    needsHeader =
                                        if (!listSelect?.get(nSelect)!!.signature && nRead == 0) { // read header
                                            true
                                        } else if (listSelect?.get(nSelect)!!.signature && nRead == 1) { // read  header
                                            true
                                        } else isCmdSignature
                                    /* Build buffer
                                    MathUtils.sHexa is used to create the C1B file */
                                    MathUtils.setHexa(
                                        listSelect?.get(nSelect)!!,
                                        isCmdSignature,
                                        needsHeader,
                                        sC1BContent
                                    )
                                }
                            } else {
                                if (listSelect?.get(nSelect)!!.commande != MF) {
                                    throw  Exception("Erreur de lecture de la carte")
                                }
                            }

                            // Reading is finished
                            if (nSelect == selectSize && nRead == readSize) {
                                sendAPDUCommandToTransmit(DF_TACHOGRAPH)
                                sendAPDUCommandToTransmit(EF_CARD_DOWNLOAD)
                                sendAPDUCommandToTransmit(setUpdate())
                                if(IS_TG2){
                                    sendAPDUCommandToTransmit(DF_TACHOGRAPH)
                                    sendAPDUCommandToTransmit(MF)
                                    sendAPDUCommandToTransmit(DF_TACHOGRAPH_G2)
                                    sendAPDUCommandToTransmit(EF_CARD_DOWNLOAD)
                                    sendAPDUCommandToTransmit(setUpdate())

                                }
                                libGauge = listSelect?.get(nSelect)!!.getNom() + " : " +
                                        Integer.toString(nRead + 1) + "/" + Integer.toString(
                                    readSize + 1
                                )
                                //onUpdateReaderView?.onUpadateMessages(
                                //    "final",
                                //    libGauge,
                                //    listSelect!!.size - 1
                                //)
                                bluetoothCardReadHasEnded = true
                                bluetoothIsReadingCard = false
                                end = true
                                createC1BFile()

                                return@runOnUiThread
                            }
                            //Lecture select suivant + configuration liste Read
                            if (!isRead) {
                                if (listSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
                                    TGUtils.setTG2Signature(sReponseCardSignCertif)
                                }
                                nSelect++
                                nRead = -1
                                // appelProcedureWL("setJauge", nSelect, selectSize + 1);
                                libGauge = listSelect?.get(nSelect)!!.nom + " : " +
                                        Integer.toString(nRead + 1) + "/" + (readSize + 1).toString()
                                //onUpdateReaderView?.onUpadateMessages(
                                //    "first",
                                //    libGauge,
                                //    listSelect!!.size - 1
                                //)
                                listRead.clear()

                                if (listSelect?.get(nSelect)!!.isEF) {
                                    listRead = TGUtils.setReadCommands(listSelect?.get(nSelect)!!)
                                }
                                readSize = listRead.size - 1
                                isRead = listRead.isNotEmpty() 

                                sendAPDUCommandToTransmit(listSelect?.get(nSelect)!!.commande)

                            }
                            // next read
                            else {
                                nRead++
                                libGauge =
                                    listSelect?.get(nSelect)!!.nom + " : " + (nRead + 1).toString() + "/" + (readSize + 1).toString()

                                //onUpdateReaderView?.onUpadateMessages(
                                //    "second",
                                //    libGauge,
                                //    listSelect!!.size - 1
                                //)

                                if ((listSelect?.get(nSelect)!! == wTG1_APP || listSelect?.get(
                                        nSelect
                                    ) == wTG2_APP)
                                    && nRead == 2
                                ) {
                                    calculateEFSize(listSelect?.get(nSelect)!!, sResponse)
                                }
                                /* Vérifier la carte conducteur si EF_IDENTIFICATION */
                                if (listSelect?.get(nSelect)!! == wTG1_ID && nRead == 2) {
                                    sReponse520 = sC1BContent
                                    Log.e("identity", sReponse520)
                                    var sTemp = sReponse520.substring(3)

                                    sTemp = StringUtils.getHexString(sTemp, 14)
                                    sTemp = sTemp.replace(" ", "")
                                    val sCardNum = StringUtils.convertHexToString(sTemp)
                                    val numCard = driver.carte

                                    Log.e("numCard", "" + numCard)
                                    if (sCardNum != numCard) {
                                        finalCardStatus = FinalCardStatus.Absent
                                        //onUpdateReaderView?.onchangeViewTo(12)
                                        return@runOnUiThread
                                    }
                                }

                                /* Sed next command to Reader */
                                if (nRead < readSize) {
                                    sendAPDUCommandToTransmit(listRead[nRead])
                                } else {
                                    isRead = false
                                    sendAPDUCommandToTransmit(listRead[nRead])
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    finalATR = ""
                    isCardInserted = false
                    bluetoothReaderIsPowered = false
                    bluetoothIsReadingCard = false
                    isReadingBluetoothCard = false
                    //onUpdateReaderView?.onchangeViewTo(7)
                    Log.e("ERROR", "" + e.localizedMessage)
                }
            }
        }
        // Wait for escape command response.
        mBluetoothReader?.setOnEscapeResponseAvailableListener { bluetoothReader, response, errorCode ->
            activity.runOnUiThread {
                Log.e("setOnEscapeResponse", "Other traitement")
            }
        }
    }
}
