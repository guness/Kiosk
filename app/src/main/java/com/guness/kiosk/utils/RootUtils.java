package com.guness.kiosk.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by guness on 02/09/16.
 */
public class RootUtils {

    private static final String TAG = RootUtils.class.getSimpleName();

    public static String runAsRoot(String... strings) throws IllegalAccessException {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        Process su;
        try {
            su = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            throw new IllegalAccessException("Permission denied, cannot run su !!!");
        }
        try {
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + "\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(outputStream, response);
        }
        return res;
    }

    private static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    private static void closeSilently(Closeable... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Closeable x : xs) {
            if (x != null) {
                try {
                    Log.d(TAG, "closing: " + x);
                    x.close();
                } catch (Throwable e) {
                    Log.e(TAG, "There is an error while closing an object", e);
                }
            }
        }
    }
}