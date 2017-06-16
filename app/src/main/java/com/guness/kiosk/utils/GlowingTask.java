package com.guness.kiosk.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.guness.kiosk.R;

/**
 * Created by guness on 31/10/2016.
 */

/**
 * Execute method takes 4 ImageViews in alphabetical order.
 */
public class GlowingTask extends AsyncTask<Void, Boolean, Void> {

    private static final int MAT_DURATION = 1000;
    private static final int GLOW_DURATION = 1000;
    private static final int TRANSITION = 800;

    private static final int[] NORMAL_RES = {R.drawable.bonus, R.drawable.faq, R.drawable.ranglist, R.drawable.news, R.drawable.trade};
    private static final int[] GLOW_RES = {R.drawable.bonus2, R.drawable.faq2, R.drawable.ranglist2, R.drawable.news2, R.drawable.trade2};
    private static TransitionDrawable[] mTransitions;

    private volatile boolean isPlay;

    @UiThread
    public GlowingTask(Context context, ImageView... imageViews) {
        isPlay = true;
        mTransitions = new TransitionDrawable[imageViews.length];
        for (int i = 0; i < imageViews.length; i++) {
            Drawable[] layers = new Drawable[2];
            layers[0] = ContextCompat.getDrawable(context, GLOW_RES[i]);
            layers[1] = ContextCompat.getDrawable(context, NORMAL_RES[i]);
            mTransitions[i] = new TransitionDrawable(layers);
            imageViews[i].setImageDrawable(mTransitions[i]);
        }
    }

    @UiThread
    public void stop() {
        isPlay = false;
        onProgressUpdate(false);
    }

    @Override
    protected Void doInBackground(Void... aVoid) {
        while (isPlay) {
            synchronized (this) {
                try {
                    wait(MAT_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isPlay) {
                return null;
            }

            publishProgress(true);
            synchronized (this) {
                try {
                    wait(GLOW_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isPlay) {
                return null;
            }
            publishProgress(false);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {

        for (TransitionDrawable transition : mTransitions) {
            if (values[0]) {
                transition.startTransition(TRANSITION);
            } else {
                transition.reverseTransition(TRANSITION);
            }
        }
    }
}
