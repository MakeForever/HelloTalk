package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.Arrays;

public class FriendDetailActivity extends ToolBarActivity {
    public static final String TAG = FriendDetailActivity.class.getSimpleName();
    private TextView textView;
    private TextView nameTextView;
    private User user = null;
    private String myId = null;
    private ImageView profileImageView;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_friend_detail);
        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            user = extras.getParcelable("object");
        }
        profileImageView = (ImageView) findViewById(R.id.user_profile_image_view);
        profileImageView.setImageBitmap(user.getProfileImg(this));
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString( getString(R.string.user_id), null );
        textView = (TextView) findViewById(R.id.invite_chat_button);
        nameTextView = (TextView) findViewById(R.id.friend_detail_name_text_view);
        nameTextView.setText(user.getName());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( user != null ) {
                    int chatType = 1;
                    Intent intent = new Intent( mContext, PersonalChatActivity.class );
                    boolean isSynchronized;
                    boolean isStored;
                    String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(new String [] { myId, user.getId()}));
                    ContentResolver resolver = getContentResolver();
                    Cursor chatCursor = resolver.query(
                            TalkContract.ChatRooms.CONTENT_URI,
                            new String[] { TalkContract.ChatRooms.CHAT_ROOM_TYPE, TalkContract.ChatRooms.IS_SYNCHRONIZED },
                            TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] { chatTableName },
                            null);
                    if (chatCursor.getCount() > 0 ) {
                        chatCursor.moveToFirst();
                        isSynchronized = true;
                        isStored = true;
                    } else {
                        isSynchronized = false;
                        isStored = false;
                    }
                    Log.d(TAG, "onClick: "+ chatTableName);
                    PersonalChatRoom chatRoom = new PersonalChatRoom( chatTableName, chatType, isSynchronized, user);
                    intent.putExtra("chatRoom", chatRoom);
                    intent.putExtra("is_stored", isStored);
                    startActivity(intent);
                    finish();
                    chatCursor.close();
                }

            }
        });
    }
}
