package com.guness.kiosk.models;

/**
 * Created by guness on 11/09/16.
 */
public class Command {

    public String command;
    public String result;
    public boolean asRoot;
    public boolean isExecuted;
    public boolean isFailed;

    @Override
    public String toString() {
        return "{" +
                "'command' : '" + command + "', " +
                "'asRoot':" + asRoot + ", " +
                "'result':'" + result + "', " +
                "'isExecuted':" + isExecuted + ", " +
                "'isFailed': " + isFailed + "}";
    }
}
