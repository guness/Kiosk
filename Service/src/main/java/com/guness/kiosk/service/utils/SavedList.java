package com.guness.kiosk.service.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guness on 25/06/2017.
 */

public class SavedList {
    private SharedPreferences preferences;
    private final int limit;
    private final HashMap<String, String> internalList = new HashMap<>();

    public SavedList(Context context, String name, int max) {
        this.limit = max;
        this.preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        internalList.putAll((Map<? extends String, ? extends String>) preferences.getAll());
        trimList();
    }

    private void trimList() {
        while (internalList.size() > limit) {
            internalList.remove(internalList.keySet().iterator().next());
        }
    }

    public void putValue(String key, String value) {
        if (key == null) {
            key = "NULL";
        }
        internalList.put(key, value);
        trimList();
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, String> entry : internalList.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    public boolean contains(String key) {
        if (key == null) {
            key = "NULL";
        }
        return internalList.keySet().contains(key);
    }

    public String getValue(String key, String defaultValue) {
        String value = internalList.get(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
