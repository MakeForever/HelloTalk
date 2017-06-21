package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.FriendAddAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

import static com.beakya.hellotalk.database.TalkContract.User.USER_ID;

public class FriendAddActivity extends AppCompatActivity {
    public static final String TAG = FriendAddActivity.class.getSimpleName();
    private SearchView searchView;
    private FriendAddAdapter friendAddAdapter;
    private RecyclerView SearchResultRecyclerView;
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initialize socket
        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        searchView = (SearchView) findViewById(R.id.searchView);

        searchView.setFocusable(true);
        searchView.requestFocus();
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                if(query.length() > 3) {
                    socket.emit("search_friends", query);
                } else {
                    friendAddAdapter.swapData(null);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                if(newText.length() > 3) {
                    socket.emit("search_friends", newText);
                } else {
                    friendAddAdapter.swapData(null);
                }
                return false;
            }
        });


        friendAddAdapter = new FriendAddAdapter(this);
        SearchResultRecyclerView = (RecyclerView) findViewById(R.id.search_result_recyclerView);
        SearchResultRecyclerView.setAdapter(friendAddAdapter);
        SearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        friendAddAdapter.swapData(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.FriendFindEvent event) {
//        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
        JSONObject obj = event.getStorage();
        User[] users;
        Uri targetUri = TalkContract.BASE_URI.buildUpon().appendEncodedPath(TalkContract.User.FRIENDS_PATH).build();
        try {
            JSONArray array = obj.getJSONArray("data");
            Log.d(TAG, "onMessageEvent: " + array);
            users = new User[array.length()];
            for ( int i = 0; i< array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                String imgStringData = o.getString("img");
                Bitmap decodedImage = null;
                if( imgStringData != null) {
                    byte[] imageBytes = Base64.decode(imgStringData, Base64.DEFAULT);
                    decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }
                User user = new User(o.getString("id"), o.getString("name"), decodedImage);
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(targetUri, null, USER_ID+"=?", new String[]{o.getString("id")}, null );

                boolean isMyFriend = false;
                while( cursor.moveToNext() ) {
                    isMyFriend = cursor.getInt(cursor.getColumnIndex(TalkContract.User.IS_MY_FRIEND)) > 0;
                }
                if( cursor.getCount() > 0 ) {
                    user.setAdded(true);
                }
                if ( isMyFriend ) {
                    user.setMyFriend(isMyFriend);
                }
                users[i] = user;
            }
            friendAddAdapter.swapData(users);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
