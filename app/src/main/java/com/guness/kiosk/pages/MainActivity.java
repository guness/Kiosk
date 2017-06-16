package com.guness.kiosk.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.VideoView;

import com.guness.kiosk.R;
import com.guness.kiosk.core.BaseActivity;
import com.guness.kiosk.services.CardReaderService;
import com.guness.kiosk.services.OverlayService;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.videoView)
    VideoView mVideoView;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(new Intent(context, TradeCenterActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO, make this configurable
        mVideoView.setVideoURI(Uri.parse(getString(R.string.url_movie)));
        mVideoView.setOnPreparedListener(mediaPlayer -> {
            mVideoView.start();
            mediaPlayer.setLooping(true);
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(CardReaderService.ACTION_CARD_ATTACHED));
    }

    @Override
    protected boolean requiresAttach() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mVideoView.stopPlayback();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(new Intent(this, CardReaderService.class));

        startService(new Intent(this, OverlayService.class));
    }

    @Override
    public void onBackPressed() {
    }
}
