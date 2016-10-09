package com.guness.kiosk.utils;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.Nullable;

import com.guness.kiosk.BuildConfig;

import java.util.HashMap;

/**
 * Created by guness on 09/10/2016.
 */

public class DeviceUtils {
    @Nullable
    public static UsbDevice getConnectedReader(UsbManager manager) {
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        for (String key : devices.keySet()) {
            UsbDevice device = devices.get(key);
            if (device.getVendorId() == BuildConfig.READER_VENDOR_ID && device.getProductId() == BuildConfig.READER_PRODUCT_ID) {
                return device;
            }
        }
        return null;
    }
}
