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
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.guness.kiosk.utils.DeviceUtils;
import com.guness.kiosk.utils.RootUtils;

import java.util.Arrays;

import static com.guness.kiosk.core.Constants.COMMAND_CLEAR_META;

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

            // Create output string
            final String outputString = "Slot " + slotNum + ": " + stateStrings[prevState] + " -> " + stateStrings[currState];

            Log.d(TAG, outputString);

        });

        mUsbReceiver = new UsbReceiver();

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
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
                        clearMetaTraderCache();
                        try {
                            mReader.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Error while closing device", e);
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                synchronized (this) {
                    if (device != null) {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }
        }
    }

    private void openDevice(UsbDevice device) {
        try {
            mReader.open(device);
            mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {
                if (currState == Reader.CARD_ABSENT) {
                    clearMetaTraderCache();
                } else if (currState == Reader.CARD_PRESENT) {
// Transmit APDU
                    byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    byte[] response = new byte[100];
                    int responseLength = 0;

                    try {
                        mReader.power(slotNum, Reader.CARD_WARM_RESET);
                        mReader.setProtocol(slotNum, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                        responseLength = mReader.transmit(0, command, command.length, response, response.length);
                    } catch (ReaderException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.e(TAG, "responseLength: " + responseLength);
                    Log.e(TAG, "response: " + Arrays.toString(response));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error while opening device", e);
        }
    }

    private void clearMetaTraderCache() {
        try {
            Log.e(TAG, "Clearing MetaCache: " + RootUtils.run(true, COMMAND_CLEAR_META));
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Clearing MetaCache Failed", e);
            e.printStackTrace();
        }
    }
}
