package com.guness.kiosk.pages;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.services.OverlayService;
import com.guness.kiosk.utils.CompatUtils;
import com.guness.kiosk.utils.DeviceUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static final String ACTION_USB_PERMISSION = "com.guness.kiosk.USB_PERMISSION";

    public static final String SETUP_COMPLETED = "SETUP_COMPLETED";
    public static final String OVERLAY_ENABLED = "OVERLAY_ENABLED";
    public static final String CARD_READER_ENABLED = "CARD_READER_ENABLED";
    public static final String SYSTEM_BARS_HIDDEN = "SYSTEM_BARS_HIDDEN";

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100001;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.overlay_button)
    Switch mOverlaySwitch;

    @BindView(R.id.card_reader)
    Switch mReaderSwitch;

    @BindView(R.id.hide_bars)
    Switch mBarSwitch;

    SharedPreferences mPrefs;

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                boolean enabled = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                mReaderSwitch.setChecked(enabled);
                mPrefs.edit().putBoolean(CARD_READER_ENABLED, enabled).apply();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice usbDevice = DeviceUtils.getConnectedReader(mUsbManager);
                if (usbDevice == null) {
                    mReaderSwitch.setChecked(false);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: " + getIntent().getAction());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        mPrefs = getSharedPreferences(null, MODE_PRIVATE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        mOverlaySwitch.setChecked(mPrefs.getBoolean(OVERLAY_ENABLED, false));
        mReaderSwitch.setChecked(mPrefs.getBoolean(CARD_READER_ENABLED, false));
        mBarSwitch.setChecked(mPrefs.getBoolean(SYSTEM_BARS_HIDDEN, false));

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        if (isFinishing()) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " + getIntent().getAction());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            boolean canDraw = CompatUtils.canDrawOverlays(this);
            mOverlaySwitch.setChecked(canDraw);
            mPrefs.edit().putBoolean(OVERLAY_ENABLED, canDraw).apply();
        }
    }

    @OnCheckedChanged(R.id.overlay_button)
    void overlayToggled(boolean checked) {
        Intent intent = new Intent(this, OverlayService.class);
        if (checked) {
            if (CompatUtils.canDrawOverlays(this)) {
                mPrefs.edit().putBoolean(OVERLAY_ENABLED, true).apply();
                startService(intent);
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent askIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(askIntent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                } else {
                    Toast.makeText(this, R.string.unexptected_behaviour, Toast.LENGTH_LONG).show();
                    mOverlaySwitch.setChecked(false);
                    mPrefs.edit().putBoolean(OVERLAY_ENABLED, false).apply();
                }
            }
        } else {
            mPrefs.edit().putBoolean(OVERLAY_ENABLED, false).apply();
            stopService(intent);
        }
    }

    @OnCheckedChanged(R.id.card_reader)
    void cardReaderToggled(boolean checked) {
        Log.e(TAG, "cardReaderToggled: " + checked);
        if (checked) {
            UsbDevice usbDevice = DeviceUtils.getConnectedReader(mUsbManager);
            if (usbDevice == null) {
                mReaderSwitch.setChecked(false);
                Log.e(TAG, "setChecked: " + false);
                mPrefs.edit().putBoolean(CARD_READER_ENABLED, false).apply();
            } else {
                if (mUsbManager.hasPermission(usbDevice)) {
                    Log.e(TAG, "setChecked: " + true);
                    mPrefs.edit().putBoolean(CARD_READER_ENABLED, true).apply();
                } else {
                    Log.e(TAG, "setChecked: asked");
                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                }
            }
        } else {
            Log.e(TAG, "setChecked: " + false);
            mPrefs.edit().putBoolean(CARD_READER_ENABLED, false).apply();
        }
    }

    @OnCheckedChanged(R.id.hide_bars)
    void hideBarsToggled(boolean checked) {
        //UsbConstants
    }
}
