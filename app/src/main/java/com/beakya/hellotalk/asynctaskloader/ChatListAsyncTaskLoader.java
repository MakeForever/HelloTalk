package com.beakya.hellotalk.asynctaskloader;


import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.beakya.hellotalk.contentproviders.UserContentProvider;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.receiver.ChatListReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatListAsyncTaskLoader extends AsyncTaskLoader<List<ChatRoom>> {
    private static final String TAG = ChatListAsyncTaskLoader.class.getSimpleName();
    final PackageManager packageManager;
    ChatListReceiver chatListReceiver;
    List<ChatRoom> chatRoomList;
    public ChatListAsyncTaskLoader(Context context) {
        super(context);
        this.packageManager  = getContext().getPackageManager();
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading: ");
        super.onStartLoading();
        if( chatRoomList != null ) {
            deliverResult(chatRoomList);
        }

        if ( chatListReceiver == null ) {
            chatListReceiver = new ChatListReceiver(this);
        }
    }


    @Override
    public List<ChatRoom> loadInBackground() {
        return new ArrayList<ChatRoom>();
    }

    @Override
    public void deliverResult(List<ChatRoom> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset: ");
        super.onReset();
        onStartLoading();
        if( chatListReceiver != null ) {
            getContext().unregisterReceiver(chatListReceiver);
            chatListReceiver = null;
        }
    }

    @Override
    public void onCanceled(List<ChatRoom> data) {
        super.onCanceled(data);
    }

    private void releaseResources( List<ChatRoom> data ) {

    }
}
