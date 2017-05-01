package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private String userId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("TableName");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = getContentResolver();
                String tableName = Utils.sha256(userId);
                Uri uri = TalkContract.Chat.generateCreateTableUri(tableName);
                resolver.query( uri, null, null, null, null );


                ContentValues table = new ContentValues();
                table.put("table", tableName );
                table.put("chat_list_id", tableName );

                resolver.insert(TalkContract.Chat.CONTENT_URI, table);

                //TODO: 테이블을 만들고 chatlist에 해당 테이블명을 insert한것까지 했다. 해당 테이블에 데이터를 직접 넣어야 한다.
            }
        });
    }
}
