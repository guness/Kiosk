package com.guness.kiosk.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

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
        oView = new LinearLayout(this);
        oView.setBackgroundColor(0x88ff0000); // The translucent red color
        params = new WindowManager.LayoutParams(
                200,
                200,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(oView, params);
        oView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick");
            }
        });
        oView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                oView.setOnTouchListener(OverlayService.this);
                return true;
            }
        });
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
