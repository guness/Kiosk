package com.guness.kiosk.service.utils.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by guness on 19/06/2017.
 */

public abstract class DataChangeListener implements ValueEventListener {

    public static final String TAG = DataChangeListener.class.getSimpleName();

    public abstract void call(DataSnapshot dataSnapshot);

    @Override
    public final void onDataChange(DataSnapshot dataSnapshot) {
        call(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.i(TAG, "onCancelled ignored");
    }
}
