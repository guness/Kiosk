package com.guness.kiosk.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.core.Constants;
import com.guness.kiosk.services.CardReaderService;
import com.guness.kiosk.services.OverlayService;
import com.guness.kiosk.utils.CompatUtils;
import com.guness.kiosk.utils.DeviceUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import eu.chainfire.libsuperuser.Shell;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static boolean isOnScreen;

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

    private volatile boolean isSelf;

    private UsbManager mUsbManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice usbDevice = DeviceUtils.getConnectedReader(mUsbManager);
                if (usbDevice == null) {
                    mReaderSwitch.setChecked(false);
                    mPrefs.edit().putBoolean(CARD_READER_ENABLED, false).apply();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: " + getIntent().getAction());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        mPrefs = getSharedPreferences(null, MODE_PRIVATE);

        isSelf = true;
        mOverlaySwitch.setChecked(mPrefs.getBoolean(OVERLAY_ENABLED, false));
        mReaderSwitch.setChecked(mPrefs.getBoolean(CARD_READER_ENABLED, false));
        mBarSwitch.setChecked(mPrefs.getBoolean(SYSTEM_BARS_HIDDEN, false));
        isSelf = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnScreen = true;
        Log.d(TAG, "onResume: " + getIntent().getAction());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        isOnScreen = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                mPrefs.edit().putBoolean(SETUP_COMPLETED, true).apply();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
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
        if (isSelf) {
            return;
        }
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
        if (isSelf) {
            return;
        }
        Log.d(TAG, "cardReaderToggled: " + checked);
        Intent intent = new Intent(this, CardReaderService.class);
        if (checked) {
            UsbDevice usbDevice = DeviceUtils.getConnectedReader(mUsbManager);
            if (usbDevice == null) {
                mReaderSwitch.setChecked(false);
                Log.d(TAG, "setChecked: " + false);
            } else {
                mPrefs.edit().putBoolean(CARD_READER_ENABLED, true).apply();
                startService(intent);
                return;
            }
        } else {
            Log.d(TAG, "setChecked: " + false);
        }
        mPrefs.edit().putBoolean(CARD_READER_ENABLED, false).apply();
        stopService(intent);
    }

    @OnCheckedChanged(R.id.hide_bars)
    void hideBarsToggled(CompoundButton switchButton, boolean checked) {
        if (isSelf) {
            return;
        }
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                switchButton.setEnabled(false);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                //And probably this: "pm " + (checked ? "enable" : "disable") + " com.android.launcher3"
                List<String> result = Shell.SU.run(checked ? Constants.Commands.COMMAND_DISABLE_SYSTEMUI : Constants.Commands.COMMAND_ENABLE_SYSTEMUI);
                if (result == null) {
                    return false;
                } else {
                    for (String message : result) {
                        Log.d(TAG, "Result: " + message);
                    }
                    return true;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    mPrefs.edit().putBoolean(SYSTEM_BARS_HIDDEN, checked).apply();
                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle(R.string.restart)
                            .setMessage(R.string.restart_needed)
                            .setPositiveButton(R.string.restart, (dialog, which) -> {
                                if (Shell.SU.run("reboot") == null) {
                                    Toast.makeText(SettingsActivity.this, R.string.manual_reboot_required, Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton(R.string.restart_later, null).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.root_required, Toast.LENGTH_SHORT).show();
                    switchButton.setChecked(!checked);
                }
                switchButton.setEnabled(true);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
