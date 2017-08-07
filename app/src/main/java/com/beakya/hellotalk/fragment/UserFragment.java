package com.beakya.hellotalk.fragment;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.FriendAddActivity;
import com.beakya.hellotalk.activity.FriendDetailActivity;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.adapter.UserAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.SimpleDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.beakya.hellotalk.activity.PersonalChatActivity.EVENT_NEW_MESSAGE_ARRIVED;

/**
 * Created by goodlife on 2017. 5. 4..
 */

public class UserFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        UserAdapter.mOnClickListener {

    public static final String TAG = UserFragment.class.getSimpleName();

    private UserAdapter mUserAdapter;
    private RecyclerView mRecyclerView;



    Context context;
    public UserFragment() {
        // Required empty public constructor
        Log.d(TAG, "UserFragment: constructor");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: onCreateView");
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.main_content, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
//
        mUserAdapter = new UserAdapter( context, this );
        mRecyclerView.setLayoutManager(new LinearLayoutManager( context ));
        mRecyclerView.setAdapter(mUserAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        getActivity().getSupportLoaderManager().initLoader(MainActivity.ID_USER_CURSOR_LOADER, null, this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }
    @Override
    public void onPause() {
        super.onPause();
//        EventBus.getDefault().unregister(this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch ( id ) {
            case MainActivity.ID_USER_CURSOR_LOADER:
                return new CursorLoader(
                        getContext(),
                        TalkContract.User.CONTENT_URI,
                        null,
                        TalkContract.User.IS_MY_FRIEND + " = ?",
                        new String[] { "1" },
                        null );
            default :
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(Events.MessageEvent event) {
//        switch ( event.getMessage() ) {
//            case EVENT_NEW_MESSAGE_ARRIVED :
//                getActivity().getSupportLoaderManager().restartLoader(MainActivity.ID_USER_CURSOR_LOADER, null, this);
//                break;
//        }
//    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mUserAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUserAdapter.swapCursor(null);
    }


    @Override
    public void onListItemClick( User user) {
        Log.d(TAG, "onListItemClick: ");
        Intent intent = new Intent( context, FriendDetailActivity.class );
        intent.putExtra("object", user);
        startActivity(intent);
    }
    @Override
    public void onSwipeOn() {
        Snackbar.make(getActivity().findViewById(R.id.layout_for_fab_add_friend),"스와이프를 닫아주세요", Snackbar.LENGTH_SHORT).show();
    }
}
