package com.guness.kiosk.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.guness.kiosk.BuildConfig;
import com.guness.kiosk.pages.ScreenSaverActivity;
import com.guness.kiosk.utils.DeviceUtils;

import java.util.Arrays;

import static com.guness.kiosk.core.Constants.ACTION_USB_PERMISSION;


public class CardReaderService extends Service {

    private static final String TAG = CardReaderService.class.getSimpleName();

    public static final String ACTION_CARD_ATTACHED = "CardReaderService_cardAttached";
    public static final String ACTION_CARD_DETACHED = "CardReaderService_cardDetached";

    private static final String[] stateStrings = {"Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    private UsbManager mUsbManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private UsbReceiver mUsbReceiver;
    private PowerManager.WakeLock mWakeLock;

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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_CARD_DETACHED));

                startActivity(
                        new Intent(this, ScreenSaverActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                );
            } else if (currState == Reader.CARD_PRESENT) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_CARD_ATTACHED));

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

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID);
        mWakeLock.acquire();
        new UsbLooper().start();
/*


        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        UsbDevice device = DeviceUtils.getConnectedReader(usbManager);
        if (device != null) {
            isAttached = true;
            usbManager.requestPermission(device, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
            mSnackbar.dismiss();
            finish();
        } else {
            mSnackbar.show();
        }*/
    }

    private void killMetaTrader(Context context) {

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
        mWakeLock.release();
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

    private void openDevice(UsbDevice device) {
        try {
            Log.e(TAG, "openDevice");
            mReader.open(device);
        } catch (Exception e) {
            Log.e(TAG, "Error while opening device", e);
        }
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
                        killMetaTrader(CardReaderService.this);
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

    private class UsbLooper extends Thread {
        private boolean isPlay = true;

        @Override
        public void run() {
            do {
                UsbDevice device = DeviceUtils.getConnectedReader(mUsbManager);
                if (device != null) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (isPlay);
        }
    }
}
