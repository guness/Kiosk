package com.guness.kiosk.webservice;

import com.guness.kiosk.webservice.util.RequestWrapper;
import com.guness.kiosk.webservice.util.ResponseWrapper;
import com.guness.kiosk.webservice.network.RetrieveRequest;
import com.guness.kiosk.webservice.network.RetrieveResponse;
import com.guness.kiosk.webservice.network.UpdateRequest;
import com.guness.kiosk.webservice.network.UpdateResponse;
import com.guness.kiosk.webservice.network.ValidateRequest;
import com.guness.kiosk.webservice.network.ValidateResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;


/**
 * Created by guness on 27/06/2017.
 */

public interface CardService {

    @POST("cards/retrieve")
    Observable<ResponseWrapper<RetrieveResponse>> retrieveCardData(@Body RequestWrapper<RetrieveRequest> request);

    @POST("cards/update/rfid")
    Observable<ResponseWrapper<UpdateResponse>> updateCard(@Body RequestWrapper<UpdateRequest> request);

    @POST("cards/validate")
    Observable<ResponseWrapper<ValidateResponse>> validateCard(@Body RequestWrapper<ValidateRequest> request);
}
