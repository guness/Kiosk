package com.guness.kiosk.service.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.guness.kiosk.service.BuildConfig;
import com.guness.kiosk.service.core.Constants;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class InteractionService extends Service {

    private static final String TAG = InteractionService.class.getName();

    public InteractionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Deprecated
    private static void wipeMetaTrader() {
        List<String> result = Shell.SU.run(Constants.Commands.COMMAND_WIPE_META);
        if (result == null) {
            Log.e(TAG, "MetaWipe Failed");
        } else {
            for (String message : result) {
                Log.d(TAG, "MetaWipe: " + message);
            }
        }
    }

    public static void killMetaTrader(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> activities = manager.getRunningAppProcesses();
        for (int iCnt = 0; iCnt < activities.size(); iCnt++) {
            ActivityManager.RunningAppProcessInfo info = activities.get(iCnt);
            if (info.processName.contains(Constants.META_PACKAGE)) {

                List<String> result = Shell.SU.run("kill -9 " + info.pid);
                if (result == null) {
                    Log.e(TAG, "MetaKilling Failed");
                } else {
                    for (String message : result) {
                        Log.d(TAG, "Result: " + message);
                    }
                }
            }
        }
        wipeMetaTrader();
    }


    public static void clearMetaCache() {
        Log.e(TAG, "Clearing MetaCache");
        List<String> result = Shell.SU.run(Constants.Commands.COMMANDS_CLEAR_META);
        if (result == null) {
            Log.e(TAG, "Could not clear MetaCache");
        } else {
            for (String message : result) {
                Log.d(TAG, "Result: " + message);
            }
        }
    }

    @Deprecated
    public static void grantUsbPermission(UsbDevice usbDevice) throws RemoteException {
        IBinder b = ServiceManager.getService(USB_SERVICE);
        IUsbManager service = IUsbManager.Stub.asInterface(b);
        service.setDevicePackage(usbDevice, BuildConfig.APPLICATION_ID, Process.myUid());
        service.grantDevicePermission(usbDevice, Process.myUid());
    }
}
