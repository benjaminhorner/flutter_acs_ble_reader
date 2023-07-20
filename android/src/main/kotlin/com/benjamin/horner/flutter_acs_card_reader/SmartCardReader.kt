package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.SmartCardInitializer
import com.benjamin.horner.flutter_acs_card_reader.OnUpdateConnectionState

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.content.Context
import android.app.Activity
import android.bluetooth.BluetoothManager

// ACS
import com.acs.smartcard.Reader
import com.acs.bluetooth.BluetoothReader
import com.acs.bluetooth.BluetoothReaderManager
import com.acs.bluetooth.BluetoothReaderGattCallback
import com.acs.bluetooth.Acr1255uj1Reader
import com.acs.bluetooth.Acr3901us1Reader

private val smartCardInitializer = SmartCardInitializer()

class SmartCardReader {
    private var mBluetoothReader: BluetoothReader? = null
    private var mBluetoothReaderManager: BluetoothReaderManager? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattCallback: BluetoothReaderGattCallback? = null
    private var bluetoothReaderIsPowered: Boolean = false
    private var needsAuthentication: Boolean = false
    private var isReadingBluetoothCard: Boolean = false
    private var bluetoothCardReadisFinisged: Boolean = false
    private var isReading: Boolean = false
    private var mCanStart: Boolean = false
    private var isCardInserted: Boolean = false
    private var isFinished: Boolean = false
    private var onUpdateConnectionState: OnUpdateConnectionState? = null
    private var mConnectState = BluetoothReader.STATE_DISCONNECTED
    private var FINAL_MASTER_KEY: String? = null
    private var FINAL_APDU_COMMAND: String? = null
    private var FINAL_ESCAPE_COMMAND: String? = null
    private var Final_CardStatus = ""  // L'ETAT FINAL de la Carte (Present,Powered,Absent,PowerSaving)
    private var Final_ATR: String? = null // Byte [] ATR Values

    fun readSmartCard(device: BluetoothDevice, activity: Activity, context: Context): String {
        mGattCallback = BluetoothReaderGattCallback()
        mBluetoothReaderManager = BluetoothReaderManager()

        mGattCallback!!.setOnConnectionStateChangeListener { gatt, state, newState ->
            try {
                    //onUpdateConnectionState?.updateState(state, gatt.connect(), newState)

                    if (state != BluetoothGatt.GATT_SUCCESS) {
                        mConnectState = BluetoothReader.STATE_DISCONNECTED
                        if (newState == BluetoothReader.STATE_CONNECTED) {
                            Log.e("Gatt Callback", "newState is BluetoothReader.STATE_CONNECTED")
                        } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                            Log.e("Gatt Callback", "newState is BluetoothReader.STATE_DISCONNECTED")
                            //UpdateConnectionState(newState)
                        }
                        reset()
                        isReadingBluetoothCard = false
                        isReading = false

                        isCardInserted = false
                        bluetoothReaderIsPowered = false
                        isFinished = true
                    }
                    //UpdateConnectionState(newState)
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
                        //UpdateConnectionState(newState)

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
            if (!isFinished) {
                mBluetoothReader = reader
                //setListeners(activity)
                activateReader()
            }
        }

        connectReader(device, activity)

        val data = "SmartCard data"

        return data
    }

    private fun activateReader() {
        if (mBluetoothReader == null) {
            return
        }
        if (mBluetoothReader is Acr3901us1Reader) {
            Log.e("mBluetoothReader", "Binding")
            (mBluetoothReader as Acr3901us1Reader).startBonding()
        } else if (mBluetoothReader is Acr1255uj1Reader) {
            Log.e("mBluetoothReader", "Notification")
            (mBluetoothReader as? Acr1255uj1Reader)?.enableNotification(true)
        }
    }

    private fun disconnectReader() {
        if (mBluetoothGatt == null) {
            //bLectureCard = false
            //isReading = false
            return
        }
        mBluetoothGatt?.disconnect()
        //bLectureCard = false
        //isReading = false
    }

    private fun connectReader(device: BluetoothDevice, activity: Activity) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (bluetoothManager == null) {
            return
        }
        val bluetoothAdapter = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
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
        mBluetoothGatt = device.connectGatt(activity.applicationContext, false, mGattCallback)
    }

    private fun updateConnectionState(onUpdateConnectionState: OnUpdateConnectionState) {
        this.onUpdateConnectionState = onUpdateConnectionState
    }

    private fun reset() {
        FINAL_MASTER_KEY = null
        FINAL_APDU_COMMAND = null
        FINAL_ESCAPE_COMMAND = null
        Final_CardStatus = ""
        Final_ATR = null
    }

//    private fun setListeners(activity: Activity) {
//        val mContext = activity.applicationContext

//        mBluetoothReader?.setOnAuthenticationCompleteListener { bluetoothReader, errorCode ->

//            if (errorCode == BluetoothReader.ERROR_SUCCESS) {
//                Authenticate = true

//                powerOnCard()
//                authenticationSuccess = true


//            } else {
//                // Authentication()
//                Log.e("Authentication", "Fail : " + errorCode)
//                if(!authenticationSuccess){
//                    Log.e("Authentication", "Failed to authenticate (authenticationSuccess == false) : " + errorCode)
//                }

//                if(mBluetoothReader != null){
//                    val escapeCommand = getEditTextinHexBytes("30 00")
//                    mBluetoothReader?.transmitEscapeCommand(escapeCommand)
//                }
//                Authenticate = false
//            }
//        }

//        mBluetoothReader?.setOnCardStatusChangeListener { bluetoothReader, sta ->
//            if (sta != oldState && !Authenticate) {
//                getCardStatus(sta)
//            }
//            oldState =sta
//        }

//        //setOnEnableNotificationCompleteListener
//        mBluetoothReader?.setOnEnableNotificationCompleteListener { bluetoothReader, result ->
//            if (result != BluetoothGatt.GATT_SUCCESS) {
//                /* Fail */
//                Toast.makeText(
//                    mContext, "The device is unable to set notification!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                /*L'appareil est connecté et prêt à être utilisé */
//                // mBluetoothReader?.getDeviceInfo(BluetoothReader.DEVICE_INFO_SERIAL_NUMBER_STRING)
//                Authentication() //On s'identifie
//            }
//        }

//        mBluetoothReader?.setOnDeviceInfoAvailableListener { bluetoothReader, infoId, bytes, status ->
//            if (status != BluetoothGatt.GATT_SUCCESS) {
//                Log.e("AvailableListener", "Info : ")
//            }
//        }

//        /* Handle on slot status available. */
//        mBluetoothReader?.setOnCardStatusAvailableListener { bluetoothReader, cardStatus, errorCode ->
//            activity.runOnUiThread(Runnable {
//                if (errorCode != BluetoothReader.ERROR_SUCCESS) {
//                    getErrorToString(errorCode)

//                    Log.e("AvailableListener", "Not : " + getErrorToString(errorCode))
//                } else {
//                    Log.e("AvailableListener", "can : ")
//                    getCardStatus(cardStatus)
//                    Authentication() //On s'identifie
//                }
//            })
//        }

//        /* Wait for receiving ATR string. */
//        mBluetoothReader?.setOnAtrAvailableListener { bluetoothReader, atr, errorCode ->

//            Log.e("AtrAvailableListener", "Done : " + errorCode)
//            activity.runOnUiThread {
//                if (atr == null) {
//                    //mTxtATR.setText(getErrorString(errorCode))
//                    Final_ATR = null
//                    Log.e("AtrAvailableListener", "Not : " + getErrorToString(errorCode))
//                    if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
//                        onUpdateReaderView?.onchangeViewTo(5)
//                    }

//                    bLectureCard = false
//                    isReading = false

//                } else {
//                    //PowerOnCard()
//                    Final_ATR = toHexString(atr)
//                    bFinLectureCard = false
//                    bLectureCard = true
//                    CardInserted = true
//                    isReading = true
//                    onUpdateReaderView?.onchangeViewTo(8)
//                    nSelect = 0
//                    setListeCommandeSelect()

//                    Log.e("cmd", "--> " + listeSelect?.get(nSelect)?.commande)
//                    mBluetoothReader?.transmitApdu(listeSelect?.get(nSelect)?.commande?.let {
//                        getEditTextinHexBytes(
//                            it
//                        )
//                    })

//                }
//            }
//        }

//        mBluetoothReader?.setOnCardPowerOffCompleteListener { bluetoothReader, result ->
//            activity.runOnUiThread {

//                Log.e("PowerOffListener", "Not : " + getErrorToString(result))
//                getErrorToString(result) //mTxtATR.setText(getErrorString(result))
//            }
//        }


//        //Déclaration variables for APDULISTENER
//        var tailleRead = 0
//        var tailleSelect = 0
//        var isLectureRead = false
//        var isCmd_signature = false
//        var needEnTete = false
//        var listeRead = ArrayList<String>()
//        var nRead = -1
//        var sContenuC1B = ""
//        var libJauge = ""
//        var TG2_tailleCertif = 0
//        var sReponseCardSignCertif = ""
//        var fin = false
//        var nTG1 = 0
//        var nTG2 = 0

//        mBluetoothReader?.setOnResponseApduAvailableListener { bluetoothReader, apdu, errorCode ->

//            activity.runOnUiThread {

//                //Vérification du statut de la carte
//                if (Final_CardStatus != "Active" && mBluetoothReader == null) {
//                    return@runOnUiThread
//                }

//                try {
//                    Log.e("Commnde", "" + listeSelect?.get(nSelect)?.commande)
//                    sReponse = getResponseString(apdu, errorCode)


//                    isReading = true
//                    val removeSpce = sReponse.replace(" ", "")
//                    val result = CommunUtils.convertHexToString(removeSpce)

//                    //réponse envoyé par le lecteur de carte
//                    // appelProcedureWL("EcritLog",sReponse+RC+listeSelect?.get(nSelect)!!.getCommand e())
//                    //[30/09/2019]CA UPDATE CARD DOWNLOAD
//                    /*if (nTG1 >= 2 && nTG2 >= 2) { //update
//                        if (listeSelect?.get(nSelect)?.commande.equals(EF_CARD_DOWNLOAD) && nRead == -1) {
//                            listeRead.clear()
//                            listeRead.add(setUpdate())
//                            tailleRead = listeRead.size - 1
//                            //appelProcedureWL("InfoWL",listeRead+RC+sReponse)
//                        }
//                    }*/

//                    if (!fin) {

//                        if (listeSelect?.get(nSelect)?.nom?.contains("certificate")!! && listeSelect?.get(
//                                nSelect
//                            )!!.tG2
//                        ) {
//                            if (nRead == 0) {
//                                TG2_tailleCertif = (sReponse.length / 3) - 2
//                                if (sReponse.length >= 768) { //taille > 256 (256*3char = 768char)
//                                    isLectureRead = true
//                                    listeRead.add("00 B0 00 FE 00")
//                                    //décalage de 254 (sReponse ne possède pas 90 00)
//                                } else {
//                                    listeSelect?.get(nSelect)!!.taille = TG2_tailleCertif
//                                }
//                                if (listeSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
//                                    sReponseCardSignCertif = sReponse
//                                }
//                            } else if (nRead == 1) {
//                                TG2_tailleCertif += (sReponse.length / 3) - 2
//                                listeSelect?.get(nSelect)!!.taille = TG2_tailleCertif
//                                if (listeSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
//                                    sReponseCardSignCertif += " $sReponse"
//                                }
//                            }
//                        }

//                        if (nSelect == 0) {
//                            //passage 1
//                            if (sReponse.trim() == "90 00") { //carte chrono G2{
//                                IS_TG2 = true
//                                listeSelect?.addAll(setListeTG2())
//                            } else {
//                                IS_TG2 = false
//                            }
//                          //  listeSelect?.addAll(setListeUPDATE())
//                            /*IS_OK = true;*/
//                            tailleSelect = listeSelect!!.size - 1

//                            //appelProcedureWL("setJaugeMin", 0); //Initialisation de la jauge

//                            //appelProcedureWL("setJaugeMax", tailleSelect + 1);
//                            //Initialisation de la jauge
//                            nSelect++
//                            nRead = -1
//                            nTG1 = 0
//                            nTG2 = 0
//                            TIMESTAMP = 0
//                            isLectureRead = false
//                            clicSend(listeSelect?.get(nSelect)!!.commande)

//                            Log.e("ApduAvailable", "Comm1 : ${listeSelect?.get(nSelect)?.commande}")
//                        } else {
//                            //Traitement normal
//                            if (sReponse.trim().endsWith("90 00")) {
//                                sContenuC1B = sReponse.substring(
//                                    0,
//                                    sReponse.length - 6
//                                ) //suppression "90 00" a la fin de sReponse
//                                if (sContenuC1B.replace("\\s", "").isNotEmpty()) {
///* La commande en cours est une signature */
//                                    isCmd_signature = listeRead[nRead].startsWith("00 2A 9E 9A")
///* La commande en cours a besoin d'un tête */
//                                    needEnTete =
//                                        if (!listeSelect?.get(nSelect)!!.signature && nRead == 0) { //en-tête read
//                                            true
//                                        } else if (listeSelect?.get(nSelect)!!.signature && nRead == 1) { //en-tête read
//                                            true
//                                        } else isCmd_signature
///* Construction du buffer
//sHexa utiliser pour créer le fichier C1B */
//                                    setsHexa(
//                                        listeSelect?.get(nSelect)!!,
//                                        isCmd_signature,
//                                        needEnTete,
//                                        sContenuC1B
//                                    )
//                                }
//                                //Erreur traitement
//                            } else {
//                            //appelProcedureWL("InfoWL",sReponse);
//                                if (listeSelect?.get(nSelect)!!.commande != MF) {

//                                    throw  Exception("Erreur de lecture de la carte")
//                                }
//                            }

//                            //Fin de la lecture
//                            if (nSelect == tailleSelect && nRead == tailleRead) {
//                                clicSend(DF_TACHOGRAPH)
//                                clicSend(EF_CARD_DOWNLOAD)
//                                clicSend(setUpdate())
//                                if(IS_TG2){
//                                    clicSend(DF_TACHOGRAPH)
//                                    clicSend(MF)
//                                    clicSend(DF_TACHOGRAPH_G2)
//                                    clicSend(EF_CARD_DOWNLOAD)
//                                    clicSend(setUpdate())

//                                }
//                                libJauge = listeSelect?.get(nSelect)!!.getNom() + " : " +
//                                        Integer.toString(nRead + 1) + "/" + Integer.toString(
//                                    tailleRead + 1
//                                )
//                                // appelProcedureWL("setJaugeLib", libJauge);
//                                //appelProcedureWL("setJauge", nSelect + 1, tailleSelect + 1);
//                                onUpdateReaderView?.onUpadateMessages(
//                                    "final",
//                                    libJauge,
//                                    listeSelect!!.size - 1
//                                )
//                                bFinLectureCard = true
//                                bLectureCard = false
//                                fin = true
//                                creation_c1b()

//                                return@runOnUiThread
//                            }
////Lecture select suivant + configuration liste Read
//                            if (!isLectureRead) {
///*[30/09/2019]CA UPDATE CARD DOWNLOAD*/
//                               /* if (listeSelect?.get(nSelect)!!.commande == DF_TACHOGRAPH) {
//                                    nTG1++
//                                } else if (listeSelect?.get(nSelect)!!.commande == DF_TACHOGRAPH_G2) {
//                                    nTG2++
//                                }*/
///*[11/09/2019]CA TG2 Taille signature */
//                                if (listeSelect?.get(nSelect)!!.commande == EF_CARDSIGNCERTIFICATE) {
//                                    setTG2Signature(sReponseCardSignCertif)
//                                }
//                                nSelect++
//                                nRead = -1
//                                // appelProcedureWL("setJauge", nSelect, tailleSelect + 1);
//                                libJauge = listeSelect?.get(nSelect)!!.nom + " : " +
//                                        Integer.toString(nRead + 1) + "/" + (tailleRead + 1).toString()
//                                onUpdateReaderView?.onUpadateMessages(
//                                    "first",
//                                    libJauge,
//                                    listeSelect!!.size - 1
//                                )
//                                listeRead.clear()

//                                if (listeSelect?.get(nSelect)!!.isEF) {
//                                    listeRead = setCommandesRead(listeSelect?.get(nSelect)!!)
//                                }
//                                tailleRead = listeRead.size - 1
//                                isLectureRead = listeRead.isNotEmpty() //if
//                                // appelProcedureWL("SetCommand", listeSelect?.get(nSelect)!!. getCommande());
//                                //  appelProcedureWL("ClicSend");

//                                clicSend(listeSelect?.get(nSelect)!!.commande)

//                            }
//                            //Lecture read suivant
//                            else {
//                                nRead++
//                                libJauge =
//                                    listeSelect?.get(nSelect)!!.nom + " : " + (nRead + 1).toString() + "/" + (tailleRead + 1).toString()

//                                // appelProcedureWL("setJaugeLib", libJauge);
//                                onUpdateReaderView?.onUpadateMessages(
//                                    "second",
//                                    libJauge,
//                                    listeSelect!!.size - 1
//                                )

//                                /*Récupérer les nombres de relevé de la structure de données de la carte*/
//                                if ((listeSelect?.get(nSelect)!! == wTG1_APP || listeSelect?.get(
//                                        nSelect
//                                    ) == wTG2_APP)
//                                    && nRead == 2
//                                ) {
//                                    calculTailleEF(listeSelect?.get(nSelect)!!, sReponse)
//                                }
//                                /*Vérifier la carte conducteur si EF_IDENTIFICATION*/
//                                if (listeSelect?.get(nSelect)!! == wTG1_ID && nRead == 2) {
//                                    sReponse520 = sContenuC1B
//                                    Log.e("identi", sReponse520)
//                                    var sTemp = sReponse520.substring(3)

//                                    sTemp = getHexString(sTemp, 14)
//                                    sTemp = sTemp.replace(" ", "")
//                                    val sNumCarte = convertHexToString(sTemp)
//                                    val driver = Hawk.get("USER_DATA") as User
//                                    val numCard = driver.carte

//                                    Log.e("numCard", "" + numCard)
//                                    if (sNumCarte != numCard) {
//                                        Final_CardStatus = "Absent"
//                                        onUpdateReaderView?.onchangeViewTo(12)
//                                        return@runOnUiThread
//                                    }
//                                }

//                                /*Commande suivante envoyé au lecteur*/
//                                if (nRead < tailleRead) { //cmd suivante de la liste read

//                                    clicSend(listeRead[nRead])
//                                } else { //dernière cmd de la liste read{

//                                    isLectureRead = false
//                                    clicSend(listeRead[nRead])
//                                }
//                            }
//                        }

//                    }
//                } catch (e: Exception) {
//                    Final_ATR = ""
//                    CardInserted = false
//                    BLecteurPowered = false
//                    bLectureCard = false
//                    isReading = false
//                    onUpdateReaderView?.onchangeViewTo(7)
//                    Log.e("ERROR", "" + e.localizedMessage)
//                }
//            }
//        }
//        // Wait for escape command response.
//        mBluetoothReader?.setOnEscapeResponseAvailableListener { bluetoothReader, response, errorCode ->
//            activity.runOnUiThread {
//                Log.e("setOnEscapeResponse", "Other traitement")
//            }
//        }
//    }

    //fun Authentication(): Boolean {
    //    Authenticate = false

    //    val masterKey = CommunUtils.getEditTextinHexBytes(FINAL_MASTER_KEY!!)

    //    Log.e("masterKey", "" + masterKey.toString())
    //    if (masterKey != null && masterKey.isNotEmpty()) {

    //        if (mBluetoothReader != null && mBluetoothReader?.authenticate(masterKey)!!) {
    //            Log.e("Authentication", "En cours")
    //            onUpdateReaderView?.onUpadateMAJUI("LIB_Jumelage", "gsAuthentificationEncours", 1)
    //        }

    //    }
    //    return Authenticate
    //}
}
