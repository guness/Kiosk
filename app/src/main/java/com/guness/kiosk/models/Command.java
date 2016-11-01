package com.guness.kiosk.models;

import java.util.Arrays;
import java.util.List;

/**
 * Created by guness on 11/09/16.
 */
public class Command {

    public String command;
    public List<String> result;
    public boolean asRoot;
    public boolean isExecuted;
    public boolean isFailed;

    @Override
    public String toString() {
        return "{" +
                "'command' : '" + command + "', " +
                "'asRoot':" + asRoot + ", " +
                "'result[]':'" + Arrays.toString(result.toArray()) + "', " +
                "'isExecuted':" + isExecuted + ", " +
                "'isFailed': " + isFailed + "}";
    }
}
