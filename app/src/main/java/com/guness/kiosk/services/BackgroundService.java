package com.guness.kiosk.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.guness.kiosk.utils.RootUtils;

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service @ onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "service @ onCreate");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Executing commands");
                    String result = RootUtils.runAsRoot("setprop service.adb.tcp.port 5555", "stop adbd", "start adbd", "getprop service.adb.tcp.port");
                    Log.d(TAG, "Commands executed successfully: " + result);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Cannot run start-up commands", e);
                }
            }
        }, 10000);
    }
}
