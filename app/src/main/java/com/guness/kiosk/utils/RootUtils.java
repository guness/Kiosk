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

    public static String run(boolean asRoot, String... strings) throws IllegalAccessException {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        Process process;
        try {
            process = Runtime.getRuntime().exec(asRoot ? "su" : "sh");
        } catch (IOException e) {
            throw new IllegalAccessException("Permission denied, cannot run su !!!");
        }
        try {
            outputStream = new DataOutputStream(process.getOutputStream());
            response = process.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + "\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
            if (res.endsWith("\n")) {
                res = res.substring(0, res.length() - 1);
            }
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
