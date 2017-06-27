package com.guness.kiosk.service.core;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.guness.kiosk.service.receivers.BootReceiver;
import io.fabric.sdk.android.Fabric;

/**
 * Created by guness on 11/06/2017.
 */

public class ServiceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        BootReceiver.startServices(this);
    }
}
