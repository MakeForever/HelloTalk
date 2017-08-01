package com.beakya.hellotalk.fragment;

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

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.adapter.ChatListAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatListAsyncTaskLoader;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatListItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.beakya.hellotalk.activity.GroupChatActivity.EVENT_INVITED_USER;
import static com.beakya.hellotalk.activity.PersonalChatActivity.EVENT_NEW_MESSAGE_ARRIVED;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<ChatListItem>> {
    public static final String TAG = ChatListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ChatListAdapter mChatListAdapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(MainActivity.ACTION_CHAT_LIST_ASYNC, null, this);
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
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.ACTION_CHAT_LIST_ASYNC, null, this);
        EventBus.getDefault().register(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Loader<ArrayList<ChatListItem>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        switch ( id ) {
            case MainActivity.ACTION_CHAT_LIST_ASYNC :
                return new ChatListAsyncTaskLoader(getContext());
            default :
                throw new RuntimeException("no match id");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.UserInviteEvent event) {
        switch ( event.getMessage() ) {
            case EVENT_INVITED_USER :
                getActivity().getSupportLoaderManager().restartLoader(MainActivity.ACTION_CHAT_LIST_ASYNC, null, this);
                break;
            default :
                throw new RuntimeException("message not matched : " + event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch ( event.getMessage() ) {
            case EVENT_NEW_MESSAGE_ARRIVED :
                getActivity().getSupportLoaderManager().restartLoader(MainActivity.ACTION_CHAT_LIST_ASYNC, null, this);
                break;
//            default :
//                throw new RuntimeException("message not matched message : " + event.getMessage());
        }
    }
    @Override
    public void onLoadFinished(Loader<ArrayList<ChatListItem>> loader, ArrayList<ChatListItem> data) {
        switch ( loader.getId() ) {
            case MainActivity.ACTION_CHAT_LIST_ASYNC:
                mChatListAdapter.swapData(data);
                break;
            default:
                throw new RuntimeException("not matched id");
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ChatListItem>> loader) {
        mChatListAdapter.swapData(null);
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
