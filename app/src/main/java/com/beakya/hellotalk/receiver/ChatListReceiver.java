package com.beakya.hellotalk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.beakya.hellotalk.asynctaskloader.ChatListAsyncTaskLoader;

/**
 * Created by goodlife on 2017. 5. 13..
 */

public class ChatListReceiver extends BroadcastReceiver {
    private static final String TAG = ChatListReceiver.class.getSimpleName();
    public static final String ACTION_CHAT_UPDATE = "chat_list_receiver_chat_update";
    public static final String ACTION_CHAT_ROOM_USER_UPDATE = "chat_list_receiver_room_user_update";

    final ChatListAsyncTaskLoader loader;

    public ChatListReceiver( ChatListAsyncTaskLoader loader ) {
        this.loader = loader;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHAT_UPDATE);
        filter.addAction(ACTION_CHAT_ROOM_USER_UPDATE);
        loader.getContext().registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: success");
    }
}
