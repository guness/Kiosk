package com.guness.kiosk.webservice.network;

import com.guness.kiosk.webservice.model.ApplicationContext;

/**
 * Created by guness on 27/06/2017.
 */

public class RequestWrapper<T extends RequestObject> {

    final ApplicationContext Context = new ApplicationContext();

    final T RequestObject;

    public RequestWrapper(T requestObject) {
        RequestObject = requestObject;
    }
}
