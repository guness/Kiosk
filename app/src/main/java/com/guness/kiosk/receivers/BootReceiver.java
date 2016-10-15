package com.guness.kiosk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.guness.kiosk.services.BackgroundService;
import com.guness.kiosk.services.CardReaderService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        startServices(context);
    }

    public static void startServices(Context context) {
        context.startService(new Intent(context, BackgroundService.class));
    }
}
