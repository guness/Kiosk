package com.guness.kiosk.webservice.manager;

import com.guness.kiosk.webservice.CardService;
import com.guness.kiosk.webservice.model.Card;
import com.guness.kiosk.webservice.network.*;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by guness on 15/06/2017.
 */
public class WebServiceManager {
    private static final WebServiceManager ourInstance = new WebServiceManager();

    public static WebServiceManager getInstance() {
        return ourInstance;
    }

    private CardService mCardService;

    private WebServiceManager() {
    }

    public void init() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://52.28.91.8:9091")
                .build();
        mCardService = retrofit.create(CardService.class);
    }

    public Observable<ValidateResponse> validateCard(String number, String secret, String rfid) {
        ValidateRequest request = new ValidateRequest(number, secret, rfid);
        return mCardService.validateCard(new RequestWrapper<>(request)).map(ResponseWrapper::getData);
    }

    public Observable<ValidateResponse> validateCard(Card card) {
        ValidateRequest request = new ValidateRequest(card.Number, card.Secret, card.RFID);
        return mCardService.validateCard(new RequestWrapper<>(request)).map(ResponseWrapper::getData);
    }

    public Observable<UpdateResponse> updateCard(String number, String rfid) {
        UpdateRequest request = new UpdateRequest(number, rfid);
        return mCardService.updateCard(new RequestWrapper<>(request)).map(ResponseWrapper::getData);
    }

    public Observable<UpdateResponse> updateCard(Card card) {
        UpdateRequest request = new UpdateRequest(card.Number, card.RFID);
        return mCardService.updateCard(new RequestWrapper<>(request)).map(ResponseWrapper::getData);
    }

    public Observable<RetrieveResponse> retrieveCard(String bankId, String accountNumber) {
        RetrieveRequest request = new RetrieveRequest(bankId, accountNumber);
        return mCardService.retrieveCardData(new RequestWrapper<>(request)).map(ResponseWrapper::getData);
    }
}
