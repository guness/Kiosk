package com.guness.kiosk.core;

/**
 * Created by guness on 20/10/2016.
 */

public class Constants {
    public static final String META_PACKAGE = "net.metaquotes.metatrader4";

    public static class Commands {
        public static final String[] COMMANDS_CLEAR_META = {
                "rm -rf /data/data/net.metaquotes.metatrader4/cache",
                "rm -rf /data/data/net.metaquotes.metatrader4/files",
                "rm -rf /data/data/net.metaquotes.metatrader4/shared_prefs"};
    }
}
