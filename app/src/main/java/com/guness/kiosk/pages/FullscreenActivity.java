package com.guness.kiosk.pages;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.receivers.BootReceiver;
import com.guness.kiosk.services.CardReaderService;
import com.guness.kiosk.services.OverlayService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.guness.kiosk.pages.SettingsActivity.OVERLAY_ENABLED;
import static com.guness.kiosk.pages.SettingsActivity.SETUP_COMPLETED;

public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = FullscreenActivity.class.getSimpleName();

    private static final String APP_METATRADER4 = "net.metaquotes.metatrader4";

    private static final int UI_ANIMATION_DELAY = 300;
    private static final int ACTION_SETTINGS = 1001;

    @BindView(R.id.fullscreen_content)
    View mContentView;

    @BindView(R.id.fullscreen_content_controls)
    View mControlsView;

    @BindView(R.id.settings)
    View mSettingsButton;

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

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
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
        mContentView.setSoundEffectsEnabled(false);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        delayedHide(100);

        BootReceiver.startServices(this);
        IntentFilter filter = new IntentFilter("BB");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, OverlayService.class));
        if (mPrefs.getBoolean(SettingsActivity.CARD_READER_ENABLED, false)) {
            startService(new Intent(this, CardReaderService.class));
        } else {
            stopService(new Intent(this, CardReaderService.class));
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mPrefs.getBoolean(SETUP_COMPLETED, false)) {
            onSettingsClicked();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPrefs.getBoolean(OVERLAY_ENABLED, false)) {
            startService(new Intent(this, OverlayService.class));
        }
        if (isFinishing()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @OnClick(R.id.settings)
    void onSettingsClicked() {
        startActivity(new Intent(FullscreenActivity.this, SettingsActivity.class));
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
}
