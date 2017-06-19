package com.guness.kiosk.service.model;

import java.util.List;

/**
 * Created by guness on 11/09/16.
 */
public class Device {
    String name;
    List<Command> commands;
    long lastOnline;

    public String getName() {
        return name;
    }
}
