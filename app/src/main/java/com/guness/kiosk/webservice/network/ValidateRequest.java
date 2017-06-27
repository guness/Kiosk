package com.guness.kiosk.webservice.network;

/**
 * Created by guness on 27/06/2017.
 */

public class ValidateRequest extends RequestObject {

    String Number;
    String Secret;
    String RFID;

    public ValidateRequest(String number, String secret, String RFID) {
        Number = number;
        Secret = secret;
        this.RFID = RFID;
    }
}
