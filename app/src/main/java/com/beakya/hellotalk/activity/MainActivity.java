package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.MainAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private MainAdapter mUserAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton faButton;
    private static final int ID_USER_CURSOR_LOADER = 1;
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String token = Utils.getToken(this);
        if( token == null ) {
//        if(true) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.friends_recyclerView);
        faButton = (FloatingActionButton) findViewById(R.id.fab);
        mUserAdapter = new MainAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mUserAdapter);
        getSupportLoaderManager().initLoader(ID_USER_CURSOR_LOADER, null, this);

        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FriendAddActivity.class);
                startActivity(intent);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete

                // COMPLETED (1) Construct the URI for the item to delete
                //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                // Retrieve the id of the task to delete
                Log.d(TAG, "onSwiped: " + swipeDir);
            }
        }).attachToRecyclerView(mRecyclerView);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch ( id ) {
            case ID_USER_CURSOR_LOADER:
                return new CursorLoader( this, TalkContract.Friend.CONTENT_URI, null, null, null, null );
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
}
