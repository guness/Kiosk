package com.guness.kiosk.webservice.util;

import com.guness.kiosk.webservice.model.Item;
import com.guness.kiosk.webservice.model.ServiceException;
import com.guness.kiosk.webservice.network.ResponseObject;
import io.reactivex.functions.Function;

import java.util.List;

public class ResponseMapper<T extends ResponseObject> implements Function<ResponseWrapper<T>, T> {

    @Override
    public T apply(ResponseWrapper<T> wrapper) throws Exception {
        ServiceException exception = wrapper.getException();
        if (exception != null) {
            List<Item> items = exception.getItems();
            if (items != null && items.size() > 0) {
                throw new UnsupportedOperationException(items.get(0).getUIMessage());
            }
        }
        T response = wrapper.getData();
        if (response == null) {
            throw new IllegalArgumentException("Empty response with no error");
        }
        return response;
    }
}