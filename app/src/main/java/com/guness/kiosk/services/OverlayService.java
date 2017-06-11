package com.guness.kiosk.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.guness.kiosk.R;
import com.guness.kiosk.pages.TradeCenterActivity;
import com.guness.kiosk.utils.CompatUtils;

public class OverlayService extends Service {

    private static final String TAG = OverlayService.class.getSimpleName();

    public static final String ACTION_ONCLICK = "OverlayService_onClick";

    private ImageView oView;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (oView != null) {
                switch (intent.getAction()) {
                    case TradeCenterActivity.ACTION_ONRESUME:
                        oView.setImageResource(R.drawable.t1);
                        oView.setEnabled(false);
                        break;
                    case TradeCenterActivity.ACTION_ONMETA:
                        oView.setImageResource(R.drawable.t2);
                        break;
                    case TradeCenterActivity.ACTION_ONPAUSE:
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
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    300,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.END | Gravity.BOTTOM;
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            oView.setPadding(50, 50, 50, 50);
            wm.addView(oView, params);
            oView.setOnClickListener(view -> {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONCLICK));
                startActivity(
                        new Intent(OverlayService.this, TradeCenterActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                );
            });
            oView.setEnabled(false);
            IntentFilter filter = new IntentFilter();
            filter.addAction(TradeCenterActivity.ACTION_ONPAUSE);
            filter.addAction(TradeCenterActivity.ACTION_ONRESUME);
            filter.addAction(TradeCenterActivity.ACTION_ONMETA);
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
}
