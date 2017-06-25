package com.guness.kiosk.service.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by guness on 11/09/16.
 */
public class Device {
    public String name;
    public List<Command> commands;
    public long lastOnline;
    public HashMap<String, String> globalCommandResults;
}
