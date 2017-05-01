package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.MainAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketTask;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MainAdapter.mOnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private MainAdapter mUserAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton faButton;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ImageView navigationDrawerImageView;
    private TextView headerNameTextView;
    private TextView headerEmailTextView;
    private static final int ID_USER_CURSOR_LOADER = 1;
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        boolean token = Utils.checkToken(this);
        if( !token ) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navigationDrawerImageView = (ImageView) headerView.findViewById(R.id.navigation_header_image_view);
        headerNameTextView = (TextView) headerView.findViewById(R.id.navigation_header_name_text_view);
        headerEmailTextView = (TextView) headerView.findViewById(R.id.navigation_header_email_text_view);
        SharedPreferences userInfoStorage = getSharedPreferences(getString(R.string.user_info), MODE_PRIVATE);
        String myName = userInfoStorage.getString(getString(R.string.user_name), null);
        String myId = userInfoStorage.getString((getString(R.string.user_id)), null);
        if( myName != null ) {
            headerNameTextView.setText(myName);
        }
        if( myId != null ) {
            headerEmailTextView.setText(myId);
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.friends_recyclerView);
        faButton = (FloatingActionButton) findViewById(R.id.fab);

        String fileName = getString(R.string.setting_profile_img_name);
        String extension = getString(R.string.setting_profile_img_extension);
        String directory = getString(R.string.setting_profile_img_directory);

        Bitmap profileBitmap = Utils.getImageBitmap(this, fileName, extension, Arrays.asList(directory));
        if( profileBitmap != null ) {
            navigationDrawerImageView.setImageBitmap(profileBitmap);
        }

        faButton.setImageResource(R.drawable.ic_add_black_24dp);
        mUserAdapter = new MainAdapter(this, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mUserAdapter);

        getSupportLoaderManager().initLoader(ID_USER_CURSOR_LOADER, null, this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

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
                return new CursorLoader( this, TalkContract.User.CONTENT_URI, null, null, null, null );
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
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "menu test", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
            socketIntent.setAction(SocketTask.ACTION_SOCKET_DISCONNECT);
            startService(socketIntent);
            boolean success = Utils.logout(this);
            if( true ) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListItemClick( String tableName ) {
        Intent intent = new Intent( this, ChatActivity.class );
        intent.putExtra("TableName", tableName);
        startActivity(intent);
    }
}
