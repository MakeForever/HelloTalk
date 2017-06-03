package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.ViewPagerAdapter;
import com.beakya.hellotalk.fragment.ChatListFragment;
import com.beakya.hellotalk.fragment.UserFragment;
import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketTask;
import com.beakya.hellotalk.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int ACTION_CHAT_LIST_ASYNC = 0;
    public static final int ID_USER_CURSOR_LOADER = 1;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ImageView navigationDrawerImageView;
    private TextView headerNameTextView;
    private TextView headerEmailTextView;
    private ViewPager viewPager;
    MenuItem prevMenuItem;
    BottomNavigationView bottomNavigationView;
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean token = Utils.checkToken(this);
        if( !token ) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        String test = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onCreate: " + test);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        navigationDrawerImageView = (ImageView) headerView.findViewById(R.id.navigation_header_image_view);
        headerNameTextView = (TextView) headerView.findViewById(R.id.navigation_header_name_text_view);
        headerEmailTextView = (TextView) headerView.findViewById(R.id.navigation_header_email_text_view);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        SharedPreferences userInfoStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        String myName = userInfoStorage.getString(getString(R.string.user_name), null);
        String myId = userInfoStorage.getString((getString(R.string.user_id)), null);
        if( myName != null ) {
            headerNameTextView.setText(myName);
        }
        if( myId != null ) {
            headerEmailTextView.setText(myId);
        }


        String fileName = getString(R.string.setting_profile_img_name);
        String extension = getString(R.string.setting_profile_img_extension);
        String directory = getString(R.string.setting_profile_img_directory);

        Bitmap profileBitmap = Utils.getImageBitmap(this, fileName, extension, Arrays.asList(directory));
        navigationDrawerImageView.setImageBitmap(profileBitmap);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();



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
            if( success ) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager( ViewPager viewPager ) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        UserFragment userFragment = new UserFragment();
        ChatListFragment chatListFragment = new ChatListFragment();
        adapter.addFragment(userFragment,getString(R.string.tab_name_friend));
        adapter.addFragment(chatListFragment,getString(R.string.tab_name_chats));
        viewPager.setAdapter(adapter);
    }
}
