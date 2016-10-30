package com.guness.kiosk.receivers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.guness.kiosk.services.CardReaderService;

/**
 * Created by guness on 22/10/2016.
 */

public class AttachReceiver extends Activity {

    private static final String TAG = AttachReceiver.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, CardReaderService.class);
        startService(intent);
        Log.e(TAG, "USB Attach Event Caught, starting service.");
        finish();
    }
}