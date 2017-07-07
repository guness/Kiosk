package com.guness.kiosk.service.utils.firebase;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by guness on 25/06/2017.
 */

public class FirebaseKeys {
    public static final String LAST_ONLINE = "lastOnline";
    public static final String MANUFACTURER = "manufacturer";
    public static final String PRODUCT = "product";
    public static final String UUID = "UUID";
    public static final String DEVICES = "devices";
    public static final String IS_EXECUTED = "isExecuted";
    public static final String COMMANDS = "commands";
    public static final String JOBS = "jobs";
    public static final String COMMAND = "command";
    public static final String GLOBAL_COMMANDS = "globalCommands";
    public static final String GLOBAL_JOBS = "globalJobs";
    public static final String GLOBAL_COMMAND_RESULTS = "globalCommandResults";
    public static final String GLOBAL_JOB_RESULTS = "globalCommandResults";
    public static final String IS_FAILED = "isFailed";
    public static final String RESULT = "result";
    public static final String EXECUTING = "EXECUTING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String SCHEDULED = "SCHEDULED";
    public static final String STATUS = "status";
    public static final String TYPE = "type";


    public static final String TYPE_INSTALL = "TYPE_INSTALL";
    public static final String TYPE_UNINSTALL = "TYPE_UNINSTALL";

    @StringDef({TYPE_INSTALL, TYPE_UNINSTALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface JobTypes {
    }
}
