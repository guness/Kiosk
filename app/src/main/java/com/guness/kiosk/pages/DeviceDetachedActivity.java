package com.guness.kiosk.pages;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.guness.kiosk.R;
import com.guness.kiosk.core.BaseActivity;
import com.guness.kiosk.utils.DeviceUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guness.kiosk.core.Constants.ACTION_USB_PERMISSION;

public class DeviceDetachedActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.content)
    View mContentView;

    private Snackbar mSnackbar;
    private boolean isAttached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detached);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);

        mSnackbar = Snackbar.make(mContentView, R.string.connect_cardreader, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, v -> {
            UsbDevice device = DeviceUtils.getConnectedReader(usbManager);
            if (device != null) {
                isAttached = true;
                usbManager.requestPermission(device, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
                mSnackbar.dismiss();
                finish();
            } else {
                mSnackbar.show();
            }
        }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (!isAttached) {
                    mSnackbar.show();
                }
            }
        });
        mSnackbar.show();
    }

    @Override
    public void onBackPressed() {
    }
}
