package com.guness.kiosk.service.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.guness.kiosk.service.model.Job;
import com.guness.kiosk.service.utils.JobHelper;
import com.guness.kiosk.service.utils.firebase.DataChangeListener;

import static com.google.firebase.database.FirebaseDatabase.getInstance;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.DEVICES;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.FAILED;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.GLOBAL_JOBS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.JOBS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.RESULT;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.STATUS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.SUCCESS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.UUID;

public class JobExecutorService extends JobService {
    public static final int JOB_ID = 10101;
    private DatabaseReference mGlobalJobsRef;
    private DatabaseReference mJobsRef;

    public JobExecutorService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String localUUID = prefs.getString(UUID, null);
        if (TextUtils.isEmpty(localUUID)) {
            return false;
        } else {
            mGlobalJobsRef = getInstance().getReference(GLOBAL_JOBS).child(JOBS);
            mGlobalJobsRef.addListenerForSingleValueEvent(new DataChangeListener() {
                @Override
                public void call(DataSnapshot dataSnapshot) {
                    for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                        final Job job = JobHelper.getJob(jobSnapshot);
                        if (job != null && JobHelper.isToBeExecuted(job)) {
                            JobHelper.execute(job, (success, message) -> {
                                jobSnapshot.child(STATUS).getRef().setValue(success ? SUCCESS : FAILED);
                                jobSnapshot.child(RESULT).getRef().setValue(message);
                                jobFinished(jobParameters, false);
                            });
                        } else {
                            jobFinished(jobParameters, false);
                        }
                    }
                }
            });

            mJobsRef = getInstance().getReference(DEVICES).child(localUUID).child(JOBS);
            mJobsRef.addListenerForSingleValueEvent(new DataChangeListener() {
                @Override
                public void call(DataSnapshot dataSnapshot) {
                    for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                        final Job job = JobHelper.getJob(jobSnapshot);
                        if (job != null && JobHelper.isToBeExecuted(job)) {
                            JobHelper.execute(job, (success, message) -> {
                                jobSnapshot.child(STATUS).getRef().setValue(success ? SUCCESS : FAILED);
                                jobSnapshot.child(RESULT).getRef().setValue(message);
                                jobFinished(jobParameters, false);
                            });
                        } else {
                            jobFinished(jobParameters, false);
                        }
                    }
                }
            });
            return true;
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
