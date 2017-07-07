package com.guness.kiosk.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.guness.kiosk.service.services.BackgroundService;
import com.guness.kiosk.service.services.InteractionService;


public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        startServices(context);
    }

    public static void startServices(Context context) {
        context.startService(new Intent(context, InteractionService.class));
        context.startService(new Intent(context, BackgroundService.class));
    }
}
