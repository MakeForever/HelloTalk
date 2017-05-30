package com.beakya.hellotalk.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beakya.hellotalk.utils.ChatTask;

import org.json.JSONException;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatService extends IntentService {
    public static final String TAG = ChatService.class.getSimpleName();
    public ChatService() {
        super("ChatService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            new ChatTask().task( intent, this );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
