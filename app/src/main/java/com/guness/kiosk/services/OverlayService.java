package com.guness.kiosk.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.guness.kiosk.utils.CompatUtils;

public class OverlayService extends Service implements View.OnTouchListener {

    private static final String TAG = OverlayService.class.getSimpleName();

    private View oView;
    private WindowManager.LayoutParams params;
    private WindowManager wm;

    float pX, pY;
    float tmp = Float.MAX_VALUE;

    public OverlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (CompatUtils.canDrawOverlays(this)) {
            oView = new LinearLayout(this);
            oView.setBackground(new ColorDrawable(0x2F000000));
            params = new WindowManager.LayoutParams(
                    200,
                    200,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(oView, params);
            oView.setOnClickListener(view -> Log.e(TAG, "onClick"));
            oView.setOnLongClickListener(view -> {
                oView.setOnTouchListener(OverlayService.this);
                return true;
            });

        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (oView != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(oView);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                if (tmp == Float.MAX_VALUE) {
                    pX = event.getRawX();
                    pY = event.getRawY();
                    tmp = 0;
                } else {
                    tmp = pX;
                    pX = event.getRawX();
                    params.x += pX - tmp;
                    tmp = pY;
                    pY = event.getRawY();
                    params.y += pY - tmp;
                    //TODO: limit params such that overlay should not go out of screen
                    wm.updateViewLayout(oView, params);
                }
                break;
            case MotionEvent.ACTION_UP:
                oView.setOnTouchListener(null);
                tmp = Float.MAX_VALUE;
                break;
            default:
                return false;
        }
        return true;
    }
}
