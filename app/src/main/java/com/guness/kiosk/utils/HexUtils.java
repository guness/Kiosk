package com.guness.kiosk.utils;

/**
 * Created by guness on 27/06/2017.
 */

public class HexUtils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[j + offset] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
