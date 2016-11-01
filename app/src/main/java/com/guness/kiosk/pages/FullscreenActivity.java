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
import android.widget.ImageView;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.receivers.BootReceiver;
import com.guness.kiosk.services.CardReaderService;
import com.guness.kiosk.services.OverlayService;
import com.guness.kiosk.utils.GlowingTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.guness.kiosk.pages.SettingsActivity.OVERLAY_ENABLED;
import static com.guness.kiosk.pages.SettingsActivity.SETUP_COMPLETED;

public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = FullscreenActivity.class.getSimpleName();
    public static final String ACTION_ONRESUME = "FullscreenActivity_onResume";
    public static final String ACTION_ONPAUSE = "FullscreenActivity_onPause";
    public static final String ACTION_ONMETA = "FullscreenActivity_onMeta";

    private static final String APP_METATRADER4 = "net.metaquotes.metatrader4";

    private static final int UI_ANIMATION_DELAY = 300;

    @BindView(R.id.content)
    View mContentView;

    @BindView(R.id.settings)
    View mSettingsButton;

    @BindView(R.id.trade)
    ImageView mTrade;

    @BindView(R.id.news)
    ImageView mNews;

    @BindView(R.id.bonus)
    ImageView mBonus;

    @BindView(R.id.jackpot)
    ImageView mJackpot;

    @BindView(R.id.faq)
    ImageView mFaq;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = this::hide;

    private GlowingTask mGlowingTask;

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
        if (mPrefs.getBoolean(SettingsActivity.CARD_READER_ENABLED, false)) {
            startService(new Intent(this, CardReaderService.class));
        } else {
            stopService(new Intent(this, CardReaderService.class));
        }
        if (mPrefs.getBoolean(OVERLAY_ENABLED, false)) {
            startService(new Intent(this, OverlayService.class));
        } else {
            stopService(new Intent(this, OverlayService.class));
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONRESUME));
        mGlowingTask = new GlowingTask(this, mBonus, mFaq, mJackpot, mNews, mTrade);
        mGlowingTask.execute();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mPrefs.getBoolean(SETUP_COMPLETED, false)) {
            //onSettingsClicked();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlowingTask.stop();
        mGlowingTask = null;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONPAUSE));
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

    @OnClick({R.id.trade, R.id.news, R.id.bonus, R.id.jackpot, R.id.faq})
    void onButtonClicked(View view) {
        String text = null;
        Intent webActivity = new Intent(this, WebActivity.class);
        switch (view.getId()) {
            case R.id.trade:
                if (launchApplication(APP_METATRADER4)) {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONMETA));
                } else {
                    text = "Cannot find selected application.";
                }
                break;
            case R.id.news:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_news)));
                break;
            case R.id.bonus:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_bonus)));
                break;
            case R.id.jackpot:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_jackpot)));
                break;
            case R.id.faq:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_faq)));
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
