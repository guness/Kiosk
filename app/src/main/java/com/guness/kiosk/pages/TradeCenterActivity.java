package com.guness.kiosk.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.guness.kiosk.R;
import com.guness.kiosk.core.BaseActivity;
import com.guness.kiosk.utils.GlowingTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TradeCenterActivity extends BaseActivity {

    private static final String TAG = TradeCenterActivity.class.getSimpleName();
    public static final String ACTION_ONRESUME = "TradeCenterActivity_onResume";
    public static final String ACTION_ONPAUSE = "TradeCenterActivity_onPause";
    public static final String ACTION_ONMETA = "TradeCenterActivity_onMeta";

    private static final String APP_METATRADER4 = "net.metaquotes.metatrader4";

    @BindView(R.id.trade)
    ImageView mTrade;

    @BindView(R.id.news)
    ImageView mNews;

    @BindView(R.id.bonus)
    ImageView mBonus;

    @BindView(R.id.ranglist)
    ImageView mRangList;

    @BindView(R.id.faq)
    ImageView mFaq;

    private GlowingTask mGlowingTask;

    private SharedPreferences mPrefs;


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_center);
        ButterKnife.bind(this);
        mPrefs = getSharedPreferences(null, MODE_PRIVATE);

        IntentFilter filter = new IntentFilter("BB");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONRESUME));
        mGlowingTask = new GlowingTask(this, mBonus, mFaq, mRangList, mNews, mTrade);
        mGlowingTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlowingTask.stop();
        mGlowingTask = null;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONPAUSE));
        if (isFinishing()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @OnClick({R.id.trade, R.id.news, R.id.bonus, R.id.ranglist, R.id.faq})
    void onButtonClicked(View view) {
        String text = null;
        Intent webActivity = new Intent(this, WebActivity.class);
        switch (view.getId()) {
            case R.id.trade:
                if (launchApplication(APP_METATRADER4)) {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ONMETA));
                } else {
                    text = "Cannot find selected application.";
                }
                break;
            case R.id.news:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_news)));
                break;
            case R.id.bonus:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_bonus)));
                break;
            case R.id.ranglist:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_ranglist)));
                break;
            case R.id.faq:
                startActivity(webActivity.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_faq)));
                break;
        }
        if (!TextUtils.isEmpty(text)) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }


    public boolean launchApplication(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
