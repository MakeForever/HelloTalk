package com.beakya.hellotalk.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.adapter.ChatListAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatListAsyncTaskLoader;
import com.beakya.hellotalk.objs.ChatRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<ChatRoom>> {
    public static final String TAG = ChatListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ChatListAdapter mChatListAdapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_chat_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mChatListAdapter = new ChatListAdapter(getContext());
        mRecyclerView.setAdapter(mChatListAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
//        getActivity().getSupportLoaderManager().initLoader(MainActivity.ACTION_CHAT_LIST_ASYNC, null, this);
    }

    @Override
    public Loader<ArrayList<ChatRoom>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        switch ( id ) {
            case MainActivity.ACTION_CHAT_LIST_ASYNC :
                return new ChatListAsyncTaskLoader(getContext());
            default :
                throw new RuntimeException("no match id");
        }

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ChatRoom>> loader, ArrayList<ChatRoom> data) {
        switch ( loader.getId() ) {
            case MainActivity.ACTION_CHAT_LIST_ASYNC:
                mChatListAdapter.swapData(data);
                break;
            default:
                throw new RuntimeException("not matched id");
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ChatRoom>> loader) {
        mChatListAdapter.swapData(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
