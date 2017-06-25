package com.guness.kiosk.service.utils.firebase;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Created by guness on 19/06/2017.
 */

public abstract class ChildAddChangeListener implements ChildEventListener {

    private static final String TAG = ChildAddChangeListener.class.getSimpleName();

    public abstract void call(DataSnapshot dataSnapshot, String s);

    @Override
    public final void onChildAdded(DataSnapshot dataSnapshot, String s) {
        call(dataSnapshot, s);
    }

    @Override
    public final void onChildChanged(DataSnapshot dataSnapshot, String s) {
        call(dataSnapshot, s);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, "onChildRemoved ignored");
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildRemoved ignored");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled ignored");
    }
}
