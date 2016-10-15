package com.guness.kiosk.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.guness.kiosk.models.Command;
import com.guness.kiosk.utils.RootUtils;

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Executing init");
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Log.d(TAG, "Result: " + RootUtils.run(true, "setprop service.adb.tcp.port 5555", "stop adbd", "start adbd", "getprop service.adb.tcp.port"));
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "adb over TCP failed", e);
                    }
                    return null;
                }
            }.execute();
        }, 10000);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String UDID = prefs.getString("UDID", null);
        if (TextUtils.isEmpty(UDID)) {
            UDID = FirebaseDatabase.getInstance().getReference("devices").push().getKey();
            prefs.edit().putString("UDID", UDID).apply();
        }
        listenCommands(UDID);
    }

    void listenCommands(String UUID) {
        Log.i(TAG, "Connected to Firebase");
        FirebaseDatabase.getInstance().getReference("devices").child(UUID).child("lastOnline").setValue(System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("devices").child(UUID).child("name").setValue(android.os.Build.MANUFACTURER + " [" + android.os.Build.PRODUCT + "]");
        FirebaseDatabase.getInstance().getReference("devices").child(UUID).child("commands").orderByChild("isExecuted").equalTo(false).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                consumeCommand(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                consumeCommand(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void consumeCommand(final DataSnapshot dataSnapshot) {
        final Command command = dataSnapshot.getValue(Command.class);
        if (command != null) {
            if (!command.isExecuted) {
                Log.e(TAG, "Consuming command: '" + command.command + "'");
                dataSnapshot.getRef().child("isExecuted").setValue(true);
                String result;
                try {
                    result = RootUtils.run(command.asRoot, command.command);
                    Log.e(TAG, "Command consumed, result: [" + result + "]");
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Command failed: '" + command.command + "'", e);
                    dataSnapshot.getRef().child("isFailed").setValue(true);
                    result = e.getMessage();
                }
                dataSnapshot.getRef().child("result").setValue(result);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
