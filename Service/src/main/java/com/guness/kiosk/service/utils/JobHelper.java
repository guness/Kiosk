package com.guness.kiosk.service.utils;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.guness.kiosk.service.model.Job;
import com.guness.kiosk.service.model.jobs.InstallJob;
import com.guness.kiosk.service.model.jobs.UninstallJob;

import java.util.ArrayList;
import java.util.List;

import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.TYPE;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.TYPE_INSTALL;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.TYPE_UNINSTALL;

/**
 * Created by guness on 02/07/2017.
 */

public class JobHelper {

    private static final String TAG = JobHelper.class.getSimpleName();

    public static void signalJob(Context context, final DataSnapshot dataSnapshot) {
        final String type = dataSnapshot.child(TYPE).getValue(String.class);
    }

    public static boolean isToBeScheduled(@NonNull Job job) {
        String status = job.status;
        return status == null;
    }

    public static boolean isToBeExecuted(Job job) {
        String status = job.status;
        return status == null;
    }

    public static Job getJob(DataSnapshot jobSnapshot) {
        final String type = jobSnapshot.child(TYPE).getValue(String.class);
        if (!TextUtils.isEmpty(type)) {
            return jobSnapshot.getValue(getClassOf(type));
        }
        return null;
    }

    public static Class<? extends Job> getClassOf(String type) {
        if (TextUtils.isEmpty(type)) {
            throw new IllegalArgumentException("Type cannot be empty");
        } else {
            switch (type) {
                case TYPE_INSTALL:
                    return InstallJob.class;
                case TYPE_UNINSTALL:
                    return UninstallJob.class;
                default:
                    Log.e(TAG, "Type is not well defined, returning default job: " + type);
                    return Job.class;
            }
        }
    }

    public static void execute(Job job, final ObJobResult listener) {
        new Handler().postDelayed(() -> {
            List<String> list = new ArrayList<>(1);
            list.add("Nicely done");
            listener.onResult(true, list);
        }, 20000);
    }

    public interface ObJobResult {
        void onResult(boolean success, List<String> message);
    }
}
