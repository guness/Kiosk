package com.guness.kiosk.service;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.inputText)
    EditText mInputText;

    @BindView(R.id.udidView)
    TextView mUdidView;

    @BindView(R.id.saveButton)
    View mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String UDID = prefs.getString("UDID", null);
        if (!TextUtils.isEmpty(UDID)) {
            mUdidView.setText(UDID);
        }
    }

    @OnClick(R.id.saveButton)
    void onSaveClicked() {

        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String UDID = prefs.getString("UDID", null);
        if (!TextUtils.isEmpty(UDID)) {
            FirebaseDatabase.getInstance().getReference("devices").child(UDID).child("name").setValue(mInputText.getText().toString().trim());
            mSaveButton.setEnabled(false);
            mInputText.setEnabled(false);
        }
    }
}