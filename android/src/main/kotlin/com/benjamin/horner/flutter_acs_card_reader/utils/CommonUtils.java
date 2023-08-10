package com.benjamin.horner.flutter_acs_card_reader;

import java.util.Locale;

public class CommonUtils {


    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the byte array.
     */


    public static byte[] toByteArray(String hexString) {

        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        //Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[len] = (byte) (value << 4);

                } else {

                    byteArray[len] |= value;
                    len++;
                }

                first = !first;
            }
        }
        return byteArray;
    }


    /**
     * Creates a hexadecimal <code>String</code> representation of the
     * <code>byte[]</code> passed. Each element is converted to a
     * <code>String</code> via the {@link Integer#toHexString(int)} and
     * separated by <code>" "</code>. If the array is <code>null</code>, then
     * <code>""<code> is returned.
     *
     * @param array the <code>byte</code> array to convert.
     * @return the <code>String</code> representation of <code>array</code> in
     * hexadecimal.
     */
    public static String toHexString(byte[] array) {

        String bufferString = "";

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String hexChar = Integer.toHexString(array[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString;
    }

    private static boolean isHexNumber(byte value) {
        return !(!(value >= '0' && value <= '9') && !(value >= 'A' && value <= 'F')
                && !(value >= 'a' && value <= 'f'));
    }

    /**
     * Checks a hexadecimal <code>String</code> that is contained hexadecimal
     * value or not.
     *
     * @param string the string to check.
     * @return <code>true</code> the <code>string</code> contains Hex number
     * only, <code>false</code> otherwise.
     * @throws NullPointerException if <code>string == null</code>.
     */
    private static boolean isHexNumber(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        boolean flag = true;

        for (int i = 0; i < string.length(); i++) {
            char cc = string.charAt(i);
            if (!isHexNumber((byte) cc)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    /**
     * Creates a <code>byte[]</code> representation of the hexadecimal
     * <code>String</code> passed.
     *
     * @param string the hexadecimal string to be converted.
     * @return the <code>array</code> representation of <code>String</code>.
     * @throws IllegalArgumentException if <code>string</code> length is not in even number.
     * @throws NullPointerException     if <code>string == null</code>.
     * @throws NumberFormatException    if <code>string</code> cannot be parsed as a byte value.
     */
    public static byte[] hexString2Bytes(String string) {
        if (string == null)
            throw new NullPointerException("string was null");

        int len = string.length();

        if (len == 0)
            return new byte[0];
        if (len % 2 == 1)
            throw new IllegalArgumentException(
                    "string length should be an even number");

        byte[] ret = new byte[len / 2];
        byte[] tmp = string.getBytes();

        for (int i = 0; i < len; i += 2) {
            if (!isHexNumber(tmp[i]) || !isHexNumber(tmp[i + 1])) {
                throw new NumberFormatException(
                        "string contained invalid value");
            }
            ret[i / 2] = uniteBytes(tmp[i], tmp[i + 1]);
        }
        return ret;
    }

    public static byte[] getEditTextinHexBytes(String rawdata) {

        if (rawdata == null || rawdata.isEmpty()) {
            return null;
        }

        String command = rawdata.replace(" ", "").replace("\n", "");

        if (command.isEmpty() || command.length() % 2 != 0
                || !isHexNumber(command)) {
            return null;
        }

        return hexString2Bytes(command);
    }

    public static String convertHexToString(String hex) {
        String ascii = "";
        String str;
// Convert hex string to "even" length
        int rmd, length;
        length = hex.length();
        rmd = length % 2;
        if (rmd == 1)
            hex = "0" + hex;
        // split into two characters
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //split the hex into pairs
            String pair = hex.substring(i, (i + 2)); //convert hex to decimal
            int dec = Integer.parseInt(pair, 16);
            str = CheckCode(dec);
//ascii=ascii+" "+str;
            ascii = ascii + str;
        }
        return ascii;
    }

    static String CheckCode(int dec) {
        String str;
//convert the decimal to character
        str = Character.toString((char) dec);
        if (dec < 32 || dec > 126 && dec < 161) str = "";//n/a";
        return str;
    }


}