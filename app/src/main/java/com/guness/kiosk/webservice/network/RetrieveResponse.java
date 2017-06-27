package com.guness.kiosk.webservice.network;

import com.guness.kiosk.webservice.model.Bank;
import com.guness.kiosk.webservice.model.Card;
import com.guness.kiosk.webservice.model.Customer;

/**
 * Created by guness on 27/06/2017.
 */

public class RetrieveResponse extends ResponseObject {
    public Card Card;
    public Bank Bank;
    public Customer Customer;
}
