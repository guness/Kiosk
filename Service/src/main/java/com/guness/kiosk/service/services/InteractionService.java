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
import com.guness.kiosk.service.ICommandService;
import com.guness.kiosk.service.core.Constants;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class InteractionService extends Service {

    private static final String TAG = InteractionService.class.getName();

    public InteractionService() {
    }

    private final ICommandService.Stub mBinder = new ICommandService.Stub() {
        @Override
        public void clearMetaCache() throws RemoteException {
            Log.e(TAG, "clearMetaCache");
            killMetaTrader(getApplicationContext());
        }

        public int getPid() {
            return Process.myPid();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return mBinder;
    }


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

    @Deprecated
    public static void grantUsbPermission(UsbDevice usbDevice) throws RemoteException {
        IBinder b = ServiceManager.getService(USB_SERVICE);
        IUsbManager service = IUsbManager.Stub.asInterface(b);
        service.setDevicePackage(usbDevice, BuildConfig.APPLICATION_ID, Process.myUid());
        service.grantDevicePermission(usbDevice, Process.myUid());
    }
}
