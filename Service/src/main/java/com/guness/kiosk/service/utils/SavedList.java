package com.guness.kiosk.service.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by guness on 25/06/2017.
 */

public class SavedList {
    private SharedPreferences preferences;
    private final int limit;
    private final String name;
    private final List<String> internalList = new ArrayList<>();

    public SavedList(Context context, String name, int max) {
        this.limit = max;
        this.name = name;
        this.preferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        internalList.addAll(preferences.getStringSet(name, new HashSet<>()));
        trimList();
    }

    private void trimList() {
        while (internalList.size() > limit) {
            internalList.remove(0);
        }
    }

    public void addString(String value) {
        if (value == null) {
            value = "NULL";
        }
        if (internalList.contains(value)) {
            return;
        }
        internalList.add(value);
        trimList();
        preferences.edit().putStringSet(name, new HashSet<>(internalList)).apply();
    }

    public boolean contains(String value) {
        if (value == null) {
            value = "NULL";
        }
        return internalList.contains(value);
    }
}
