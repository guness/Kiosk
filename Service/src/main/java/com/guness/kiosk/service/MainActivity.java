package com.guness.kiosk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.guness.kiosk.service.core.Constants;
import com.guness.kiosk.service.utils.firebase.DataChangeListener;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.inputText)
    EditText mInputText;

    @BindView(R.id.udidView)
    TextView mUdidView;

    @BindView(R.id.ipView)
    TextView mIpView;

    @BindView(R.id.saveButton)
    View mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String UUID = prefs.getString("UUID", null);
        if (!TextUtils.isEmpty(UUID)) {
            mUdidView.setText(UUID);
            FirebaseDatabase.getInstance().getReference("devices").child(UUID).child("name").addListenerForSingleValueEvent(new DataChangeListener() {
                @Override
                public void call(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    if (name != null) {
                        mInputText.setText(name);
                        mInputText.setSelection(name.length());
                    }
                }
            });
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                int ipAddress = wifiInf.getIpAddress();
                return String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            }

            @Override
            protected void onPostExecute(String ip) {
                if (ip != null) {
                    mIpView.setText(ip);
                }
            }
        }.execute();
    }

    @OnClick(R.id.saveButton)
    void onSaveClicked() {

        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String UUID = prefs.getString("UUID", null);
        if (!TextUtils.isEmpty(UUID)) {
            FirebaseDatabase.getInstance().getReference("devices").child(UUID).child("name").setValue(mInputText.getText().toString().trim());
            mSaveButton.setEnabled(false);
            mInputText.setEnabled(false);
        }
    }

    @OnClick(R.id.enableUIButton)
    void onEnableSystemUIClicked() {
        List<String> result = Shell.SU.run(Constants.Commands.COMMAND_ENABLE_SYSTEMUI);
        displayResult(result);
    }

    @OnClick(R.id.disableUIButton)
    void onDisableSystemUIClicked() {
        List<String> result = Shell.SU.run(Constants.Commands.COMMAND_DISABLE_SYSTEMUI);
        displayResult(result);
    }

    @OnClick(R.id.reboot)
    void onRebootClicked() {
        List<String> result = Shell.SU.run(Constants.Commands.COMMAND_REBOOT);
        displayResult(result);
    }


    private void displayResult(List<String> result) {
        String resultString;
        if (result == null) {
            resultString = getString(R.string.fail);
        } else if (result.size() == 0) {
            resultString = getString(R.string.success);
        } else {
            resultString = result.get(0);
        }

        Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
    }
}