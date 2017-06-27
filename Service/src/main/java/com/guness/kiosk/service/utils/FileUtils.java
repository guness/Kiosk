package com.guness.kiosk.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by guness on 27/06/2017.
 */

public class FileUtils {
    /**
     * Copies and closes both streams
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024];
        int count;
        while ((count = is.read(bytes, 0, bytes.length)) >= 0) {
            os.write(bytes, 0, count);
            os.flush();
        }
        is.close();
        os.close();
    }
}
