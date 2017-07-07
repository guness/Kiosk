package com.guness.kiosk.service.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.guness.kiosk.service.core.Constants;
import com.guness.kiosk.service.model.Command;
import com.guness.kiosk.service.model.Job;
import com.guness.kiosk.service.utils.JobHelper;
import com.guness.kiosk.service.utils.SavedList;
import com.guness.kiosk.service.utils.firebase.ChildAddChangeListener;
import com.guness.kiosk.service.utils.firebase.DataChangeListener;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static android.app.job.JobInfo.NETWORK_TYPE_ANY;
import static com.google.firebase.database.FirebaseDatabase.getInstance;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.COMMANDS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.DEVICES;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.EXECUTING;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.FAILED;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.GLOBAL_COMMANDS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.GLOBAL_COMMAND_RESULTS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.GLOBAL_JOBS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.IS_EXECUTED;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.IS_FAILED;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.JOBS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.LAST_ONLINE;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.MANUFACTURER;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.PRODUCT;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.RESULT;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.SUCCESS;
import static com.guness.kiosk.service.utils.firebase.FirebaseKeys.UUID;

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();
    private static final String COMMAND_KEYS = "COMMAND_KEYS";
    private DatabaseReference mDeviceRef;
    private SavedList mGlobalResults;
    private JobScheduler mJobScheduler;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mGlobalResults = new SavedList(this, COMMAND_KEYS, Constants.MAX_COMMANDS);
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Starting init");
                synchronized (this) {
                    try {
                        wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Executing init on background");
                List<String> result = Shell.SU.run(new String[]{
                        "setprop service.adb.tcp.port 5555",
                        "stop adbd",
                        "start adbd",
                        "getprop service.adb.tcp.port"});
                if (result == null) {
                    Log.e(TAG, "adb over TCP failed");
                } else {
                    for (String message : result) {
                        Log.d(TAG, "adb over TCP: " + message);
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        SharedPreferences prefs = getSharedPreferences(null, MODE_PRIVATE);
        String localUUID = prefs.getString(UUID, null);
        if (TextUtils.isEmpty(localUUID)) {
            localUUID = getInstance().getReference(DEVICES).push().getKey();
            prefs.edit().putString(UUID, localUUID).apply();
        }

        mDeviceRef = getInstance().getReference(DEVICES).child(localUUID);
        mDeviceRef.child(LAST_ONLINE).setValue(System.currentTimeMillis());
        mDeviceRef.child(MANUFACTURER).setValue(android.os.Build.MANUFACTURER);
        mDeviceRef.child(PRODUCT).setValue(android.os.Build.PRODUCT);

        mDeviceRef.child(LAST_ONLINE).addValueEventListener(new DataChangeListener() {
            @Override
            public void call(DataSnapshot dataSnapshot) {
                Long lastOnline = dataSnapshot.getValue(Long.class);
                if (lastOnline == null || lastOnline == 0) {
                    mDeviceRef.child(LAST_ONLINE).setValue(System.currentTimeMillis());
                }
            }
        });
        listenCommands();
        listenJobs();
    }

    void listenCommands() {
        Log.i(TAG, "Connected to Firebase");
        mDeviceRef.child(COMMANDS).orderByChild(IS_EXECUTED).equalTo(false)
                .addChildEventListener(new ChildAddChangeListener() {
                    @Override
                    public void call(DataSnapshot dataSnapshot, String s) {
                        consumeCommand(dataSnapshot);
                    }
                });

        getInstance().getReference(GLOBAL_COMMANDS).child(COMMANDS).orderByChild(IS_EXECUTED).equalTo(false)
                .addChildEventListener(new ChildAddChangeListener() {
                    @Override
                    public void call(DataSnapshot dataSnapshot, String s) {
                        consumeGlobalCommand(dataSnapshot);
                    }
                });
    }

    void listenJobs() {
        if (Boolean.TRUE) {
            return;
        }
        ComponentName jobService = new ComponentName(getPackageName(), JobExecutorService.class.getName());
        JobInfo jobInfo = new JobInfo.Builder(JobExecutorService.JOB_ID, jobService)
                .setPeriodic(10000)
                .setRequiredNetworkType(NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        if (mJobScheduler.schedule(jobInfo) == JobScheduler.RESULT_FAILURE) {
            Log.e(TAG, "Job Scheduler failed");
        }
        mDeviceRef.child(JOBS).addValueEventListener(new DataChangeListener() {
            @Override
            public void call(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Job>> genericTypeIndicator = new GenericTypeIndicator<List<Job>>() {
                };
                List<Job> jobs = dataSnapshot.getValue(genericTypeIndicator);
                if (jobs != null) {
                    for (Job job : jobs) {
                        if (JobHelper.isToBeScheduled(job)) {
                            JobHelper.signalJob(BackgroundService.this, dataSnapshot);
                            return;
                        }
                    }
                }
            }
        });


        getInstance().getReference(GLOBAL_JOBS).child(JOBS).addValueEventListener(new DataChangeListener() {
            @Override
            public void call(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Job>> genericTypeIndicator = new GenericTypeIndicator<List<Job>>() {
                };
                List<Job> jobs = dataSnapshot.getValue(genericTypeIndicator);
                if (jobs != null) {
                    for (Job job : jobs) {
                        if (JobHelper.isToBeScheduled(job)) {
                            JobHelper.signalJob(BackgroundService.this, dataSnapshot);
                            return;
                        }
                    }
                }
            }
        });
    }

    private void consumeGlobalCommand(final DataSnapshot dataSnapshot) {
        Command command;
        try {
            command = dataSnapshot.getValue(Command.class);
        } catch (DatabaseException e) {
            Log.e(TAG, "Could not consume a command");
            return;
        }
        if (command != null) {
            if (!command.isExecuted) {
                String previousResult = mGlobalResults.getValue(command.key, null);
                if (TextUtils.isEmpty(previousResult)) {
                    Log.e(TAG, "Consuming command: '" + command.command + "'");

                    DatabaseReference executing = mDeviceRef.child(GLOBAL_COMMAND_RESULTS).child(dataSnapshot.getKey());

                    mGlobalResults.putValue(command.key, EXECUTING);
                    executing.setValue(EXECUTING);

                    List<String> result;
                    if (command.asRoot) {
                        result = Shell.SU.run(command.command);
                    } else {
                        result = Shell.SH.run(command.command);
                    }


                    if (result == null) {
                        executing.setValue(FAILED);
                        mGlobalResults.putValue(command.key, FAILED);
                    } else {
                        executing.setValue(result);
                        mGlobalResults.putValue(command.key, SUCCESS);
                    }

                }
            }
        }
    }

    private void consumeCommand(final DataSnapshot dataSnapshot) {
        Command command;
        try {
            command = dataSnapshot.getValue(Command.class);
        } catch (DatabaseException e) {
            Log.e(TAG, "Could not consume a command");
            return;
        }
        if (command != null) {
            if (!command.isExecuted) {
                Log.e(TAG, "Consuming command: '" + command.command + "'");
                dataSnapshot.getRef().child(IS_EXECUTED).setValue(true);
                List<String> result;

                if (command.asRoot) {
                    result = Shell.SU.run(command.command);
                } else {
                    result = Shell.SH.run(command.command);
                }

                if (result == null) {
                    dataSnapshot.getRef().child(IS_FAILED).setValue(true);
                }

                dataSnapshot.getRef().child(RESULT).setValue(result);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
