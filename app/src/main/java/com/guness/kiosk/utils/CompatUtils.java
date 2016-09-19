package com.guness.kiosk.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by guness on 18/09/16.
 */
public class CompatUtils {

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            return true;
        }
    }
}
