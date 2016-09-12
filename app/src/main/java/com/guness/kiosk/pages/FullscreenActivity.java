package com.guness.kiosk.pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.services.BackgroundService;
import com.guness.kiosk.services.OverlayService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullscreenActivity extends AppCompatActivity {

    private static final String APP_METATRADER4 = "net.metaquotes.metatrader4";

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    @BindView(R.id.fullscreen_content)
    View mContentView;

    @BindView(R.id.fullscreen_content_controls)
    View mControlsView;

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

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        startService(new Intent(this, BackgroundService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
       // stopService(new Intent(this, OverlayService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(this, OverlayService.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onBackPressed() {
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
