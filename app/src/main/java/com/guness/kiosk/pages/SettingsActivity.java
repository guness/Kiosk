package com.guness.kiosk.pages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Switch;

import com.guness.kiosk.R;
import com.guness.kiosk.services.OverlayService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETUP_COMPLETED = "SETUP_COMPLETED";
    public static final String OVERLAY_ENABLED = "OVERLAY_ENABLED";
    public static final String CARD_READER_ENABLED = "CARD_READER_ENABLED";
    public static final String SYSTEM_BARS_HIDDEN = "SYSTEM_BARS_HIDDEN";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.overlay_button)
    Switch mOverlaySwitch;

    @BindView(R.id.card_reader)
    Switch mReaderSwitch;

    @BindView(R.id.hide_bars)
    Switch mBarSwitch;

    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mPrefs = getSharedPreferences(null, MODE_PRIVATE);

        mOverlaySwitch.setChecked(mPrefs.getBoolean(OVERLAY_ENABLED, false));
        mReaderSwitch.setChecked(mPrefs.getBoolean(CARD_READER_ENABLED, false));
        mBarSwitch.setChecked(mPrefs.getBoolean(SYSTEM_BARS_HIDDEN, false));
    }


    @OnCheckedChanged(R.id.overlay_button)
    void overlayToggled(boolean checked) {
        Intent intent = new Intent(this, OverlayService.class);
        mPrefs.edit().putBoolean(OVERLAY_ENABLED, checked).apply();
        if (checked) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    @OnCheckedChanged(R.id.card_reader)
    void cardReaderToggled(boolean checked) {

    }

    @OnCheckedChanged(R.id.hide_bars)
    void hideBarsToggled(boolean checked) {

    }
}
