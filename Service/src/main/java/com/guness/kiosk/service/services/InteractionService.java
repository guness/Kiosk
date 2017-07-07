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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.guness.kiosk.service.BuildConfig;
import com.guness.kiosk.service.ICommandService;
import com.guness.kiosk.service.ICommandServiceCallback;
import com.guness.kiosk.service.R;
import com.guness.kiosk.service.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static com.guness.kiosk.service.core.Constants.Commands;
import static com.guness.kiosk.service.core.Constants.META_PACKAGE;

public class InteractionService extends Service {

    private static final String TAG = InteractionService.class.getName();
    private static WeakReference<InteractionService> ourInstance = new WeakReference<>(null);
    private ICommandServiceCallback mCallback;
    private List<String> serviceCards;

    @Nullable
    public static InteractionService getInstance() {
        return ourInstance.get();
    }

    public InteractionService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        ourInstance = new WeakReference<>(this);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ourInstance = new WeakReference<>(this);
    }

    private final ICommandService.Stub mBinder = new ICommandService.Stub() {
        @Override
        public void setCallback(ICommandServiceCallback callback) throws RemoteException {
            mCallback = callback;
            if (serviceCards != null) {
                mCallback.setServiceCards(serviceCards);
            }
        }

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


    private static void wipeMetaTrader(Context context) {
        String owner = parseMetaOwner();
        List<String> result = Shell.SU.run(Commands.COMMAND_WIPE_META);
        if (result == null) {
            Log.e(TAG, "MetaWipe Failed");
        } else {
            for (String message : result) {
                Log.d(TAG, "MetaWipe: " + message);
            }
        }
        if (owner != null) {
            try {
                File file = File.createTempFile("mt4", null, context.getCacheDir());
                InputStream is = context.getResources().openRawResource(R.raw.mt4);
                OutputStream os = new FileOutputStream(file);
                FileUtils.copyStream(is, os);
                Shell.SU.run(Commands.COMMAND_CREATE_META_PREFS);
                List<String> ownerResult = Shell.SU.run(new String[]{Commands.CommandCopyMT4(file.getAbsolutePath()), Commands.CommandSetMetaOwner(owner)});
                if (ownerResult != null) {
                    Log.d(TAG, "Owner Result: " + Arrays.toString(ownerResult.toArray()));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating mt4.xml", e);
            }
        }
    }

    @Nullable
    private static String parseMetaOwner() {
        List<String> result = Shell.SU.run(Commands.COMMAND_GET_META_OWER);
        if (result != null && result.size() > 0) {
            String line = result.get(0);
            if (!TextUtils.isEmpty(line)) {
                String[] tokens = line.split("\\s+");
                if (tokens.length > 3) {
                    return tokens[1] + ":" + tokens[2];
                }
            }
        }
        Log.e(TAG, "Error Parsing meta owner: " + (result == null ? "null" : Arrays.toString(result.toArray())));
        return null;
    }

    public static void killMetaTrader(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> activities = manager.getRunningAppProcesses();
        for (int iCnt = 0; iCnt < activities.size(); iCnt++) {
            ActivityManager.RunningAppProcessInfo info = activities.get(iCnt);
            if (info.processName.contains(META_PACKAGE)) {

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
        wipeMetaTrader(context);
    }

    @Deprecated
    public static void grantUsbPermission(UsbDevice usbDevice) throws RemoteException {
        IBinder b = ServiceManager.getService(USB_SERVICE);
        IUsbManager service = IUsbManager.Stub.asInterface(b);
        service.setDevicePackage(usbDevice, BuildConfig.APPLICATION_ID, Process.myUid());
        service.grantDevicePermission(usbDevice, Process.myUid());
    }

    public void postServiceCards(List<String> ids) {
        serviceCards = ids;
        if (mCallback != null) {
            try {
                mCallback.setServiceCards(serviceCards);
            } catch (RemoteException e) {
                Log.e(TAG, "cannot set service cards: " + e);
            }
        }
    }
}
