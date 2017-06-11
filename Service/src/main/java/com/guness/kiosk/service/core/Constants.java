package com.guness.kiosk.service.core;

/**
 * Created by guness on 20/10/2016.
 */

public class Constants {
    public static final String META_PACKAGE = "net.metaquotes.metatrader4";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";

    public static class Commands {

        public static final String[] COMMANDS_CLEAR_META = {
                "rm -rf /data/data/net.metaquotes.metatrader4/cache",
                "rm -rf /data/data/net.metaquotes.metatrader4/files",
                "rm -rf /data/data/net.metaquotes.metatrader4/shared_prefs"};

        public static final String COMMAND_WIPE_META = "pm clear " + META_PACKAGE;
        public static final String COMMAND_DISABLE_SYSTEMUI = "pm disable " + SYSTEMUI_PACKAGE;
        public static final String COMMAND_ENABLE_SYSTEMUI = "pm enable " + SYSTEMUI_PACKAGE;
    }
}
