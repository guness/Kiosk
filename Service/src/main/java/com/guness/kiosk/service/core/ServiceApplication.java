package com.guness.kiosk.service.core;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.guness.kiosk.service.receivers.BootReceiver;

/**
 * Created by guness on 11/06/2017.
 */

public class ServiceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        BootReceiver.startServices(this);
    }
}
