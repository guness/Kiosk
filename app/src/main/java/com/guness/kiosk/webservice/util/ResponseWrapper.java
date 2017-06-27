package com.guness.kiosk.webservice.util;

import com.guness.kiosk.webservice.model.ServiceException;
import com.guness.kiosk.webservice.network.ResponseObject;

/**
 * Created by guness on 27/06/2017.
 */

public class ResponseWrapper<T extends ResponseObject> {

    T Data;
    int OperationType;
    ServiceException Exception;

    public T getData() {
        return Data;
    }

    public int getOperationType() {
        return OperationType;
    }

    public ServiceException getException() {
        return Exception;
    }
}
