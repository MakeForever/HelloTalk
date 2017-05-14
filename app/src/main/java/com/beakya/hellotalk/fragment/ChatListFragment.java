package com.beakya.hellotalk.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.asynctaskloader.ChatListAsyncTaskLoader;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.receiver.ChatListReceiver;

import java.util.List;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ChatRoom>> {
    public static final String TAG = ChatListFragment.class.getSimpleName();
    private Button testButton;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_chat_list, container, false);
        testButton = (Button) view.findViewById(R.id.button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                Intent intent = new Intent();
                intent.setAction(ChatListReceiver.ACTION_CHAT_UPDATE);
                getActivity().sendBroadcast(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<ChatRoom>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        return new ChatListAsyncTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<ChatRoom>> loader, List<ChatRoom> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<ChatRoom>> loader) {

    }
}
