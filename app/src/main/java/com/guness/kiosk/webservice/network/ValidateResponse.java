package com.guness.kiosk.webservice.network;

import com.guness.kiosk.webservice.model.CardData;

/**
 * Created by guness on 27/06/2017.
 */

public class ValidateResponse extends ResponseObject {
    boolean IsValid;
    CardData CardData;

    public boolean isValid() {
        return IsValid;
    }

    public CardData getCardData() {
        return CardData;
    }
}
