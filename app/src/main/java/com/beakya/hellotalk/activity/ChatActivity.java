package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;


public class ChatActivity extends AppCompatActivity {
    public static final String TAG = ChatActivity.class.getSimpleName();


    private Button button;
    private EditText contentEditText;
    private String tableName = null;
    private boolean isCreatedChat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tableName = extras.getString("TableName");
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(TalkContract.ChatList.CONTENT_URI, null, TalkContract.ChatList.CHAT_LIST_ID + "= ?", new String[]{ tableName }, null);
            if( cursor.getCount() > 0 ) {
                isCreatedChat = true;
            }
        }




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = getContentResolver();
                if( !isCreatedChat ) {
                    ContentValues chatListParams = new ContentValues();
                    chatListParams.put("chat_list_id", tableName );
                    resolver.insert(TalkContract.ChatList.CONTENT_URI, chatListParams);
                    isCreatedChat = true;
                }

                ContentValues chatParams = new ContentValues();
                chatParams.put(TalkContract.ChatList.CHAT_LIST_ID, tableName);
                chatParams.put(TalkContract.Chat.SPEAKER, "me");
                chatParams.put(TalkContract.Chat.MESSAGE_CONTENT, contentEditText.getText().toString());
                chatParams.put(TalkContract.Chat.MESSAGE_TYPE, TalkContract.Chat.TYPE_TEXT);
                resolver.insert(TalkContract.Chat.CONTENT_URI, chatParams);

            }
        });
    }
}
