package com.beakya.hellotalk.services;

import android.app.AlertDialog;
import android.content.Intent;

import android.util.Log;

import com.beakya.hellotalk.activity.NotifyActivity;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by goodlife on 2017. 6. 5..
 */

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(TAG, "onStartJob: ");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}