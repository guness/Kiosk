package com.guness.kiosk.core;

import android.app.Application;
import android.os.PowerManager;

import com.crashlytics.android.Crashlytics;
import com.guness.kiosk.BuildConfig;
import com.guness.kiosk.webservice.manager.WebServiceManager;

import io.fabric.sdk.android.Fabric;

/**
 * Created by guness on 12/09/16.
 */
public class KioskApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        WebServiceManager.getInstance().init();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID);
        wakeLock.acquire();
    }
}
