package com.guness.kiosk.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.guness.kiosk.R;
import com.guness.kiosk.pages.FullscreenActivity;
import com.guness.kiosk.utils.CompatUtils;

public class OverlayService extends Service implements View.OnClickListener {

    private static final String TAG = OverlayService.class.getSimpleName();


    private ImageView oView;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (oView != null) {
                switch (intent.getAction()) {
                    case FullscreenActivity.ACTION_ONRESUME:
                        oView.setImageResource(R.drawable.t1);
                        oView.setEnabled(false);
                        break;
                    case FullscreenActivity.ACTION_ONPAUSE:
                        oView.setImageResource(R.drawable.t2);
                        oView.setEnabled(true);
                        break;
                }
            }
        }
    };

    public OverlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (CompatUtils.canDrawOverlays(this)) {
            oView = new ImageView(this);
            oView.setImageResource(R.drawable.t1);
            oView.setOnClickListener(this);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    300,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.START | Gravity.TOP;
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            oView.setPadding(50, 50, 50, 50);
            wm.addView(oView, params);
            oView.setOnClickListener(view -> Log.e(TAG, "onClick"));
            IntentFilter filter = new IntentFilter();
            filter.addAction(FullscreenActivity.ACTION_ONPAUSE);
            filter.addAction(FullscreenActivity.ACTION_ONRESUME);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if (oView != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(oView);
        }
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick");
        startActivity(new Intent(this, FullscreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND));
    }
}
