package com.guness.kiosk.webservice.network;

/**
 * Created by guness on 27/06/2017.
 */

public class ResponseWrapper<T extends ResponseObject> {

    T Data;
    int OperationType;

    public T getData() {
        return Data;
    }

    public int getOperationType() {
        return OperationType;
    }
}
