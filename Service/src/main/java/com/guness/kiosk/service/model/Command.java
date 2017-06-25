package com.guness.kiosk.service.model;

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
    public String key;

    @Override
    public String toString() {
        return "Command{" +
                "command='" + command + '\'' +
                ", result=" + result +
                ", asRoot=" + asRoot +
                ", isExecuted=" + isExecuted +
                ", isFailed=" + isFailed +
                ", key='" + key + '\'' +
                '}';
    }
}
