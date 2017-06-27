package com.guness.kiosk.webservice.network;

/**
 * Created by guness on 27/06/2017.
 */

public class UpdateRequest extends RequestObject {

    String Number;
    String RFID;

    public UpdateRequest(String number, String RFID) {
        Number = number;
        this.RFID = RFID;
    }
}
