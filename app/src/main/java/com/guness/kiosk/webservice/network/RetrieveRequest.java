package com.guness.kiosk.webservice.network;

/**
 * Created by guness on 27/06/2017.
 */

public class RetrieveRequest extends RequestObject {

    String BankId;
    String AccountNumber;

    public RetrieveRequest(String bankId, String accountNumber) {
        BankId = bankId;
        AccountNumber = accountNumber;
    }
}
