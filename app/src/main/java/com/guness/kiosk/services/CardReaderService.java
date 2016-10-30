package com.guness.kiosk.services;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.guness.kiosk.BuildConfig;
import com.guness.kiosk.core.Constants;
import com.guness.kiosk.receivers.AttachReceiver;
import com.guness.kiosk.utils.DeviceUtils;
import com.guness.kiosk.utils.RootUtils;

import java.util.Arrays;
import java.util.List;

public class CardReaderService extends Service {

    private static final String TAG = CardReaderService.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "com.guness.kiosk.USB_PERMISSION";

    private static final String[] stateStrings = {"Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    private UsbManager mUsbManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private UsbReceiver mUsbReceiver;

    public CardReaderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mUsbManager);

        mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {

            if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                prevState = Reader.CARD_UNKNOWN;
            }

            if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                currState = Reader.CARD_UNKNOWN;
            }

            Log.e(TAG, "Slot " + slotNum + ": " + stateStrings[prevState] + " -> " + stateStrings[currState]);

            if (currState == Reader.CARD_ABSENT) {
                killMetaTrader(CardReaderService.this);
            } else if (currState == Reader.CARD_PRESENT) {

                byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x08};
                byte[] response = new byte[20];
                int responseLength = 0;

                try {
                    mReader.power(slotNum, Reader.CARD_WARM_RESET);
                    mReader.setProtocol(slotNum, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                    responseLength = mReader.transmit(slotNum, command, command.length, response, response.length);
                } catch (ReaderException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "responseLength: " + responseLength);
                Log.e(TAG, "response: " + Arrays.toString(response));
            }

        });

        mUsbReceiver = new UsbReceiver();

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        UsbDevice device = DeviceUtils.getConnectedReader(mUsbManager);
        if (device != null) {
            mUsbManager.requestPermission(device, mPermissionIntent);
        }
    }

    @Override
    public void onDestroy() {
        UsbDevice device = DeviceUtils.getConnectedReader(mUsbManager);
        if (device != null && device.equals(mReader.getDevice())) {
            try {
                mReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error while closing device", e);
            }
        }
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        UsbDevice device = DeviceUtils.getConnectedReader(mUsbManager);
        if (device != null) {
            mUsbManager.requestPermission(device, mPermissionIntent);
        }
        return START_STICKY;
    }

    private class UsbReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.e(TAG, "UsbReceiver action: " + action);

            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.e(TAG, "EXTRA_PERMISSION_GRANTED");
                        if (device != null) {
                            openDevice(device);
                        }
                    } else {
                        Log.e(TAG, "Permission denied.");
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {
                    if (device != null && device.equals(mReader.getDevice())) {
                        killMetaTrader(context);
                        try {
                            mReader.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Error while closing device", e);
                        }
                    }
                }
            }
        }
    }

    private void openDevice(UsbDevice device) {
        try {
            Log.e(TAG, "openDevice");
            mReader.open(device);
        } catch (Exception e) {
            Log.e(TAG, "Error while opening device", e);
        }
    }

    public static void killMetaTrader(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> activities = manager.getRunningAppProcesses();
        for (int iCnt = 0; iCnt < activities.size(); iCnt++) {
            ActivityManager.RunningAppProcessInfo info = activities.get(iCnt);
            if (info.processName.contains(Constants.META_PACKAGE)) {
                try {
                    Log.e(TAG, "Killing Meta: " + RootUtils.run(true, "kill -9 " + info.pid));
                } catch (Exception e) {
                    Log.e(TAG, "MetaKilling Failed", e);
                    e.printStackTrace();
                }
            }
        }
        clearMetaCache();
    }

    public static void clearMetaCache() {
        try {
            Log.e(TAG, "Clearing MetaCache");
            Log.e(TAG, "Result: " + RootUtils.run(true, Constants.Commands.COMMANDS_CLEAR_META));
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not clear MetaCache", e);
            e.printStackTrace();
        }
    }

    public static void grantUsbPermission(UsbDevice usbDevice) throws RemoteException {
        IBinder b = ServiceManager.getService(USB_SERVICE);
        IUsbManager service = IUsbManager.Stub.asInterface(b);
        service.setDevicePackage(usbDevice, BuildConfig.APPLICATION_ID, Process.myUid());
        service.grantDevicePermission(usbDevice, Process.myUid());
    }
}
