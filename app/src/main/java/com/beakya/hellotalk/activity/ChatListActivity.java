package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.asynctaskloader.ChatListAsyncTaskLoader;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.receiver.ChatListReceiver;

import java.util.List;

public class ChatListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<ChatRoom>> {
    public static final String TAG = ChatListActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Loader<List<ChatRoom>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        return new ChatListAsyncTaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<ChatRoom>> loader, List<ChatRoom> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<ChatRoom>> loader) {

    }
}
