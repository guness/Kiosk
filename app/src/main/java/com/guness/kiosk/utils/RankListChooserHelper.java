package com.guness.kiosk.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.guness.kiosk.R;
import com.guness.kiosk.core.KioskApplication;
import com.guness.kiosk.pages.WebActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by guness on 28/06/2017.
 */

public class RankListChooserHelper {

    private AlertDialog mDialog;
    private Intent mWebIntent;
    private Context mContext;

    public RankListChooserHelper(Context context) {
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ranklist_chooser, null);
        ButterKnife.bind(this, view);
        mDialog = new AlertDialog.Builder(context).setView(view).create();
        mWebIntent = new Intent(context, WebActivity.class);
    }

    public void show() {
        mDialog.show();
    }

    @OnClick(R.id.rankglist_menu_super)
    void onSuperClicked() {
        String number = KioskApplication.cardData.Card.Number;
        if (number != null) {
            mDialog.dismiss();
            mContext.startActivity(mWebIntent.putExtra(WebActivity.EXTRA_URL, mContext.getString(R.string.url_ranglist_super, number)));
        }
    }

    @OnClick(R.id.rankglist_menu_weekly)
    void onWeeklyClicked() {
        String number = KioskApplication.cardData.Card.Number;
        if (number != null) {
            mDialog.dismiss();
            mContext.startActivity(mWebIntent.putExtra(WebActivity.EXTRA_URL, mContext.getString(R.string.url_ranglist_weekly, number)));
        }
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}
