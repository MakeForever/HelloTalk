package com.beakya.hellotalk.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.ViewPagerAdapter;
import com.beakya.hellotalk.fragment.ChatListFragment;
import com.beakya.hellotalk.fragment.UserFragment;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.retrofit.LogoutBody;
import com.beakya.hellotalk.retrofit.LogoutService;
import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketTask;
import com.beakya.hellotalk.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;
import io.socket.client.Socket;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.beakya.hellotalk.retrofit.RetrofitCreator.retrofit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int ACTION_CHAT_LIST_ASYNC = 0;
    public static final int ID_USER_CURSOR_LOADER = 1;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ImageView navigationDrawerImageView;
    private TextView headerNameTextView;
    private TextView headerEmailTextView;
    private TabLayout tabLayout;
    NavigationView navigationView;
    private FloatingActionButton mainFabBtn;
    private FloatingActionButton addFriendFabBtn;
    private FloatingActionButton createNewChatFabBtn;
    private LinearLayout fabAddFriendLayout;
    private LinearLayout createNewChatFabLayout;
    private RevealFrameLayout revealFrameLayout;
    private boolean isFabOpen = false;
    private boolean isFabRunning = false;
    private View fabBackground;
    private Context mContext;
    private boolean is_login;
    private Socket mSocket;
    private User myInfo;
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
        mContext = this;
        String fireBaseToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onCreate: " + fireBaseToken);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        navigationDrawerImageView = (ImageView) headerView.findViewById(R.id.navigation_header_image_view);
        headerNameTextView = (TextView) headerView.findViewById(R.id.navigation_header_name_text_view);
        headerEmailTextView = (TextView) headerView.findViewById(R.id.navigation_header_email_text_view);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        SharedPreferences userInfoStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myInfo =  Utils.getMyInfo(this);
        headerNameTextView.setText(myInfo.getName());
        headerEmailTextView.setText(myInfo.getId());
        navigationDrawerImageView.setImageBitmap(myInfo.getProfileImg(this));
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        revealFrameLayout = (RevealFrameLayout) findViewById(R.id.fab_reveal_frame_layout);
        mainFabBtn = (FloatingActionButton) findViewById(R.id.main_fab);
        createNewChatFabBtn = (FloatingActionButton) findViewById(R.id.fab_create_new_group_chat);
        addFriendFabBtn = (FloatingActionButton) findViewById(R.id.fab_add_friend);
        fabAddFriendLayout = (LinearLayout) findViewById(R.id.layout_for_fab_add_friend);
        createNewChatFabLayout = (LinearLayout) findViewById(R.id.layout_for_fab_create_new_group_chat);
        fabBackground = findViewById(R.id.fab_background);
        mainFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !isFabRunning ) {
                    setFabBackground();
                    isFabRunning = true;
                }


            }
        });
        fabBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !isFabRunning ) {
                    fabClose();
                    isFabRunning = true;
                }
            }
        });
        addFriendFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FriendAddActivity.class);
                startActivity(intent);
                setFabBackground();
            }
        });
        createNewChatFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(MainActivity.this, NewChatActivity.class);
                startActivity(activityIntent);
                setFabBackground();
            }
        });

        is_login = getIntent().getBooleanExtra("is_login", false);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (isFabOpen) {
            fabClose();
        } else {
            super.onBackPressed();
        }
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
        if (id == R.id.nav_my_setting) {
            // Handle the camera action
            Intent intent = new Intent(this, MyInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
            socketIntent.setAction(SocketTask.ACTION_SOCKET_DISCONNECT);
            startService(socketIntent);

            String token = Utils.getToken(this);
            LogoutBody body = new LogoutBody(token);
            LogoutService logoutService = retrofit.create(LogoutService.class);
            Call<ResponseBody> call = logoutService.logout(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if( response.code() != 200 ) {
                        //TODO : snackbar
                    } else {
                        boolean success = Utils.logout(mContext);
                        if( success ) {
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //TODO : snackbar
                }
            });

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



    public void setFabBackground() {
        if( isFabOpen ) {
            fabClose();
        } else {
            fabOpen();
        }
    }

    void fabOpen() {
        isFabOpen = true;
        mainFabBtn.animate().rotation(45);
        createNewChatFabLayout.setVisibility(View.VISIBLE);
        fabAddFriendLayout.setVisibility(View.VISIBLE);
        createNewChatFabLayout.animate().translationY(-170);
        fabAddFriendLayout.animate().translationY(-300);

        int cx = (mainFabBtn.getLeft() + mainFabBtn.getRight()) / 2;
        int cy = (mainFabBtn.getTop() + mainFabBtn.getBottom()) / 2;
        float finalRadius = (float) Math.hypot(cx, cy);
        Animator animator = ViewAnimationUtils.createCircularReveal(fabBackground, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fabBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isFabRunning = false;
            }
        });
        animator.start();
    }
    void fabClose() {
        mainFabBtn.animate().rotation(0);
        createNewChatFabLayout.animate().translationY(0);
        fabAddFriendLayout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {


            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(!isFabOpen){
                    createNewChatFabLayout.setVisibility(View.GONE);
                    fabAddFriendLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int cx = (mainFabBtn.getLeft() + mainFabBtn.getRight()) / 2;
        int cy = (mainFabBtn.getTop() + mainFabBtn.getBottom()) / 2;
        float finalRadius = (float) Math.hypot(cx, cy);

        Animator animator = ViewAnimationUtils.createCircularReveal(fabBackground, cx, cy, finalRadius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabBackground.setVisibility(View.INVISIBLE);
                isFabRunning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
        animator.start();
        isFabOpen = false;
    }
}
