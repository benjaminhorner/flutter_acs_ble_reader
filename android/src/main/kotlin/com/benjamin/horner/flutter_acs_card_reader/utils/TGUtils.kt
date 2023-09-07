package com.benjamin.horner.flutter_acs_card_reader

import com.benjamin.horner.flutter_acs_card_reader.MathUtils

class TGUtils {
    // companion object {
    //     private val TG1_SIGNATURE: String = "00 2A 9E 9A 80" // 128 octets
    //     private var TG2_SIGNATURE: String? = null // 64..132 octets
    //     private val READ_SIZE: Int = 254

    //     fun setTG2Signature(apdu: String) {
    //         /*
    //         SI gnTailleSign < 1 ALORS gnTailleSign = 64
    //         SI Position(sChaine,Caract(0x2B)+Caract(0x24)+Caract(0x03)+Caract(0x03)+Caract(0x02)+Caract(0x08)+ Caract(0x01)+Caract(0x01)+Caract(0x07)) > 0 ALORS gnTailleSign=64
    //         SI Position(sChaine,Caract(0x2B)+Caract(0x24)+Caract(0x03)+Caract(0x03)+Caract(0x02)+Caract(0x08)+ Caract(0x01)+Caract(0x01)+Caract(0x0B)) > 0 ALORS gnTailleSign=96
    //         SI Position(sChaine,Caract(0x2B)+Caract(0x24)+Caract(0x03)+Caract(0x03)+Caract(0x02)+Caract(0x08)+ Caract(0x01)+Caract(0x01)+Caract(0x0D)) > 0 ALORS gnTailleSign=128
    //         SI Position(sChaine,Caract(0x2A)+Caract(0x86)+Caract(0x48)+Caract(0xCE)+Caract(0x3D)+Caract(0x03)+ Caract(0x01)+Caract(0x07)) > 0 ALORS gnTailleSign=64
    //         SI Position(sChaine,Caract(0x2B)+Caract(0x81)+Caract(0x04)+Caract(0x00)+Caract(0x22)) > 0 ALORS gnTailleSign=96
    //         SI Position(sChaine,Caract(0x2B)+Caract(0x81)+Caract(0x04)+Caract(0x00)+Caract(0x23)) > 0 ALORS gnTailleSign=132
    //         */
    //         var size: Int
    //         val size64 = "2B 24 03 03 02 08 01 01 07"
    //         val size96 = "2B 24 03 03 02 08 01 01 0B"
    //         val size128 = "2B 24 03 03 02 08 01 01 0D"
    //         val size64bis = "2A 86 48 CE 3D 03 01 07"
    //         val size96bis = "2B 81 04 00 22"
    //         val size132 = "2B 81 04 00 23"
    //         /**/
    //         size = if (apdu.contains(size64)) 64 else 0
    //         size = if (apdu.contains(size96)) 96 else 0
    //         size = if (apdu.contains(size128)) 128 else 0
    //         size = if (apdu.contains(size64bis)) 64 else 0
    //         size = if (apdu.contains(size96bis)) 96 else 0
    //         size = if (apdu.contains(size132)) 132 else 0
    //         size = if (size < 1) 64 else 0
    //         /**/
    //         val sizeHexa = MathUtils.decimalToHexa(size, 2)
    //         TG2_SIGNATURE = "00 2A 9E 9A $sizeHexa"
    //     }

    //     fun setReadCommands(w: Wrapper): ArrayList<String> {
    //         val list = ArrayList<String>()
    //         val total: Int
    //         val remains: Int
    //         val nSizeEF = w.taille
    //         val isSigned = w.signature
    //         val isTG2 = w.tG2
    //         var cmd_read = ""
    //         val cmd_read_instruction = "00 B0 "
    //         var cmd_read_offset = "00 00"
    //         val cmd_hash = "80 2A 90 00"
    //         val cmd_signature =
    //             if ((isTG2)) TG2_SIGNATURE else TG1_SIGNATURE //if
    //         if (isSigned) {
    //             list.add(cmd_hash)
    //         }
    //         if (nSizeEF < READ_SIZE) {
    //             cmd_read = cmd_read_instruction + cmd_read_offset + " " + MathUtils.decimalToHexa(nSizeEF,2)
    //             list.add(cmd_read)
    //         } else {
    //             total = nSizeEF / READ_SIZE
    //             remains = nSizeEF % READ_SIZE
    //             for (i in 1..total) {
    //                 cmd_read =
    //                     cmd_read_instruction + cmd_read_offset + " " + MathUtils.decimalToHexa(READ_SIZE, 2)
    //                 list.add(cmd_read)
    //                 cmd_read_offset = MathUtils.addHexa(cmd_read_offset, MathUtils.decimalToHexa(READ_SIZE, 2))
    //                 if (cmd_read_offset.length == 2) {
    //                     cmd_read_offset = "00 $cmd_read_offset"
    //                 }
    //             }
    //             cmd_read = cmd_read_instruction + cmd_read_offset + " " + MathUtils.decimalToHexa(remains, 2)
    //             list.add(cmd_read)
    //         }
    //         if (isSigned) {
    //             list.add(cmd_hash)
    //             list.add(cmd_signature!!)
    //         }
    //         if (w.nom.contains("certificate") && isTG2) {
    //             list.clear()
    //             list.add("00 B0 00 00 00") //size max renvoyé 256 si certif plus grand faire décalage avec 00
    //         }
    //         return list
    //     }
    // }
}