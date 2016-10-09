package com.guness.kiosk.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.acs.smartcard.Reader;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

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


    public static final int MSG_SAY_HELLO = 1;
    public static final int MSG_BYE = 2;

    static class MessangerHandler extends Handler {
        private final WeakReference<CardReaderService> serviceRef;
        private Set<Messenger> outboundMessangers;

        MessangerHandler(CardReaderService service) {
            serviceRef = new WeakReference<>(service);
            outboundMessangers = new HashSet<>();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    if (msg.replyTo != null) {
                        outboundMessangers.add(msg.replyTo);
                    }
                    break;
                case MSG_BYE:
                    if (msg.replyTo != null) {
                        outboundMessangers.remove(msg.replyTo);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        void sendReplay(Message message) {
            for (Messenger messenger : outboundMessangers) {
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final MessangerHandler mMessangerHandler = new MessangerHandler(this);
    private final Messenger mMessenger = new Messenger(mMessangerHandler);

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
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
    }

    @Override
    public void onDestroy() {
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

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openDevice(device);
                        }
                    } else {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                        Log.e(TAG, "Permission requested for device");
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {
                    if (device != null && device.equals(mReader.getDevice())) {
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
            mReader.open(device);
        } catch (Exception e) {
            Log.e(TAG, "Error while opening device", e);
        }
    }
}