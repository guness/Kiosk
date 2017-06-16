package com.guness.kiosk.core;

import com.guness.kiosk.ws.SCAApiContext;
import com.guness.kiosk.ws.SCABasicHttpBinding_ICardApi;
import com.guness.kiosk.ws.SCAValidateCardDataServiceRequest;
import com.guness.kiosk.ws.SCAValidateCardServiceResponse;

import java.util.UUID;

/**
 * Created by guness on 16/06/2017.
 */

public class WebServiceManager {
    private static final WebServiceManager ourInstance = new WebServiceManager();

    public static WebServiceManager getInstance() {
        return ourInstance;
    }

    SCABasicHttpBinding_ICardApi service = new SCABasicHttpBinding_ICardApi();
    SCAApiContext context = new SCAApiContext();

    private WebServiceManager() {
        context.ApplicationId = UUID.fromString("565A2AF4-6542-4468-865D-675EC361435F");
    }

    public SCAValidateCardServiceResponse validateCardData(String number, String secret) throws Exception {
        SCAValidateCardDataServiceRequest request = new SCAValidateCardDataServiceRequest();
        request.Context = context;
        request.Number = number;
        request.Secret = secret;
        return service.ValidateCardData(request);
    }
}
