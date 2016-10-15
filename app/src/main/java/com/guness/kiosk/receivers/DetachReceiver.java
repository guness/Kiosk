package com.guness.kiosk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.guness.kiosk.pages.DeviceDetachedActivity;
import com.guness.kiosk.pages.SettingsActivity;
import com.guness.kiosk.utils.DeviceUtils;

import static android.content.Context.USB_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DetachReceiver extends BroadcastReceiver {
    public DetachReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SettingsActivity.isOnScreen) {
            if (context.getSharedPreferences(null, Context.MODE_PRIVATE).getBoolean(SettingsActivity.CARD_READER_ENABLED, false)) {
                UsbManager usbManager = (UsbManager) context.getSystemService(USB_SERVICE);
                UsbDevice usbDevice = DeviceUtils.getConnectedReader(usbManager);
                if (usbDevice == null) {
                    context.startActivity(new Intent(context, DeviceDetachedActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }
    }
}
