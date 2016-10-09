package com.guness.kiosk.pages;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.services.BackgroundService;
import com.guness.kiosk.services.CardReaderService;
import com.guness.kiosk.services.OverlayService;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.guness.kiosk.pages.SettingsActivity.CARD_READER_ENABLED;
import static com.guness.kiosk.pages.SettingsActivity.OVERLAY_ENABLED;
import static com.guness.kiosk.pages.SettingsActivity.SETUP_COMPLETED;
import static com.guness.kiosk.pages.SettingsActivity.SYSTEM_BARS_HIDDEN;

public class FullscreenActivity extends AppCompatActivity implements ServiceConnection {

    private static final String APP_METATRADER4 = "net.metaquotes.metatrader4";

    private static final int UI_ANIMATION_DELAY = 300;
    private static final int ACTION_SETTINGS = 1001;

    @BindView(R.id.fullscreen_content)
    View mContentView;

    @BindView(R.id.fullscreen_content_controls)
    View mControlsView;

    @BindView(R.id.settings)
    View mSettingsButton;

    private Messenger mService = null;

    boolean mBound;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = this::hide;

    private SharedPreferences mPrefs;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);
        mPrefs = getSharedPreferences(null, MODE_PRIVATE);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(view -> hide());

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        delayedHide(100);

        startService(new Intent(this, BackgroundService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, OverlayService.class));
        if (mPrefs.getBoolean(SettingsActivity.CARD_READER_ENABLED, false)) {
            bindService(new Intent(this, CardReaderService.class), this, BIND_AUTO_CREATE);
        } else {
            if (mBound) {
                Message msg = Message.obtain(null, CardReaderService.MSG_BYE, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(this);
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mPrefs.getBoolean(SETUP_COMPLETED, false)) {
            new Handler().postDelayed(() -> startActivityForResult(new Intent(FullscreenActivity.this, SettingsActivity.class), ACTION_SETTINGS), 100);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPrefs.getBoolean(OVERLAY_ENABLED, false)) {
            startService(new Intent(this, OverlayService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_SETTINGS && resultCode == RESULT_OK) {
            mPrefs.edit().putBoolean(SETUP_COMPLETED, data.getBooleanExtra(SETUP_COMPLETED, false))
                    .putBoolean(OVERLAY_ENABLED, data.getBooleanExtra(OVERLAY_ENABLED, false))
                    .putBoolean(CARD_READER_ENABLED, data.getBooleanExtra(CARD_READER_ENABLED, false))
                    .putBoolean(SYSTEM_BARS_HIDDEN, data.getBooleanExtra(SYSTEM_BARS_HIDDEN, false)).apply();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
    }

    @OnClick(R.id.settings)
    void onSettingsClicked() {
        startActivityForResult(new Intent(FullscreenActivity.this, SettingsActivity.class), ACTION_SETTINGS);
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.button4})
    void onButtonClicked(View view) {
        String text = null;
        switch (view.getId()) {
            case R.id.button:
                if (!launchApplication(APP_METATRADER4)) {
                    text = "Cannot find selected application.";
                }
                break;
            case R.id.button2:
                startActivity(new Intent(this, MainActivity.class));
                text = "Button 2";
                break;
            case R.id.button3:
                text = "Button 3";
                break;
            case R.id.button4:
                text = "Button 3";
                break;
        }
        if (!TextUtils.isEmpty(text)) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public boolean launchApplication(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = new Messenger(service);
        mBound = true;
        Message msg = Message.obtain(null, CardReaderService.MSG_SAY_HELLO, 0, 0);
        msg.replyTo = new Messenger(new MessangerHandler(this));
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mBound = false;
    }

    static class MessangerHandler extends Handler {
        private final WeakReference<FullscreenActivity> activityRef;

        MessangerHandler(FullscreenActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
