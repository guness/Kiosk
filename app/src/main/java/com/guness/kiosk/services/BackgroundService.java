package com.guness.kiosk.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.guness.kiosk.models.Command;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

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
        Log.d(TAG, "onCreate");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Starting init");
                synchronized (this) {
                    try {
                        wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Executing init on background");
                List<String> result = Shell.SU.run(new String[]{
                        "setprop service.adb.tcp.port 5555",
                        "stop adbd",
                        "start adbd",
                        "getprop service.adb.tcp.port",
                        "adb connect localhost"});
                if (result == null) {
                    Log.e(TAG, "adb over TCP failed");
                } else {
                    for (String message : result) {
                        Log.d(TAG, "adb over TCP: " + message);
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        Command command = null;
        try {
            command = dataSnapshot.getValue(Command.class);
        } catch (DatabaseException e) {
            Log.e(TAG, "Could not consume a command");
        }
        if (command != null) {
            if (!command.isExecuted) {
                Log.e(TAG, "Consuming command: '" + command.command + "'");
                dataSnapshot.getRef().child("isExecuted").setValue(true);
                List<String> result;

                if (command.asRoot) {
                    result = Shell.SU.run(command.command);
                } else {
                    result = Shell.SH.run(command.command);
                }

                if (result == null) {
                    dataSnapshot.getRef().child("isFailed").setValue(true);
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
