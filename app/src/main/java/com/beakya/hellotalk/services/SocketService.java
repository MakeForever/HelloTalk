package com.beakya.hellotalk.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.beakya.hellotalk.utils.SocketTask;

/**
 * Created by cheolho on 2017. 4. 4..
 */

public class SocketService extends IntentService {

    public SocketService() {
        super("SocketService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        SocketTask.task(action, this);
    }
}
