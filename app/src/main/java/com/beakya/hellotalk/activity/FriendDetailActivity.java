package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class FriendDetailActivity extends AppCompatActivity {
    public static final String TAG = FriendDetailActivity.class.getSimpleName();
    private TextView textView;
    private String id = null;
    private String myId = null;
    private ImageView profileImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            id = extras.getString("id");
        }
        profileImageView = (ImageView) findViewById(R.id.user_profile_image_view);
        Bitmap bitmap = Utils.getImageBitmap(this,
                getString(R.string.setting_friends_profile_img_name),
                getString(R.string.setting_profile_img_extension),
                Arrays.asList( new String[]{ getString(R.string.setting_friends_img_directory), id }));
        if ( bitmap != null ) {
            profileImageView.setImageBitmap(bitmap);
        }
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString( getString(R.string.user_id), null );

        textView = (TextView) findViewById(R.id.invite_chat_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( id != null ) {
                    int chatType = 1;
                    ArrayList<String> receiveList = new ArrayList<String>();
                    receiveList.add(id);
                    String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(new String[] {id, myId }));
                    Log.d(TAG, "chatTableName: " + chatTableName);
                    Intent intent = new Intent( FriendDetailActivity.this, ChatActivity.class );
                    intent.putExtra(TalkContract.ChatRoom.CHAT_LIST_ID, chatTableName);
                    intent.putExtra("receiveList", receiveList);
                    intent.putExtra("chatType", chatType);
                    intent.putExtra("receiver", id );
                    startActivity(intent);
                }

            }
        });
    }
}
