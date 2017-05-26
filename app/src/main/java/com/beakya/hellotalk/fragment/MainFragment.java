package com.beakya.hellotalk.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.FriendAddActivity;
import com.beakya.hellotalk.activity.FriendDetailActivity;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.adapter.MainAdapter;
import com.beakya.hellotalk.database.TalkContract;
<<<<<<< HEAD
import com.beakya.hellotalk.objs.User;
=======
>>>>>>> 306bf88... 커스텀 asynctaskloader 추가

/**
 * Created by goodlife on 2017. 5. 4..
 */

public class MainFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MainAdapter.mOnClickListener {

    public static final String TAG = MainFragment.class.getSimpleName();

    private MainAdapter mUserAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton faButton;
    Context context;
    public MainFragment() {
        // Required empty public constructor
        Log.d(TAG, "MainFragment: constructor");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: onCreate");
        getActivity().getSupportLoaderManager().initLoader(MainActivity.ID_USER_CURSOR_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: onCreateView");
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.app_bar_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        faButton = (FloatingActionButton) view.findViewById(R.id.fab);
        faButton.setImageResource(R.drawable.ic_add_black_24dp);
        mUserAdapter = new MainAdapter( context, this );
        mRecyclerView.setLayoutManager(new LinearLayoutManager( context ));
        mRecyclerView.setAdapter(mUserAdapter);


        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendAddActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch ( id ) {
            case MainActivity.ID_USER_CURSOR_LOADER:
                return new CursorLoader( getContext(), TalkContract.User.CONTENT_URI, null, null, null, null );
            default :
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }



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
}
