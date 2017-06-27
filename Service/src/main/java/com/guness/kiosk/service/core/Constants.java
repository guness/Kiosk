package com.guness.kiosk.service.core;

/**
 * Created by guness on 20/10/2016.
 */

public class Constants {
    public static final int MAX_COMMANDS = 10;

    private static final String DATA_FOLDER = "/data/data";
    public static final String META_PACKAGE = "net.metaquotes.metatrader4";
    public static final String META_PREFS = DATA_FOLDER + "/" + META_PACKAGE + "/shared_prefs";
    public static final String META_PREFS_MT4 = META_PREFS + "/mt4.xml";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";

    public static class Commands {

        public static final String[] COMMANDS_CLEAR_META = {
                "rm -rf /data/data/net.metaquotes.metatrader4/cache",
                "rm -rf /data/data/net.metaquotes.metatrader4/files",
                "rm -rf /data/data/net.metaquotes.metatrader4/shared_prefs"};

        public static final String COMMAND_WIPE_META = "pm clear " + META_PACKAGE;
        public static final String COMMAND_GET_META_OWER = "ls -al " + DATA_FOLDER + " | grep " + META_PACKAGE;
        public static final String COMMAND_DISABLE_SYSTEMUI = "pm disable " + SYSTEMUI_PACKAGE;
        public static final String COMMAND_ENABLE_SYSTEMUI = "pm enable " + SYSTEMUI_PACKAGE;
        public static final String COMMAND_CREATE_META_PREFS = "mkdir " + META_PREFS;


        public static String CommandCopyMT4(String filePath) {
            return "cp " + filePath + " " + META_PREFS_MT4;
        }

        public static String CommandSetMetaOwner(String ownerString) {
            return "chown -R " + ownerString + " " + META_PREFS;
        }
    }
}
