package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.ChatAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.events.MessageEvent;
import com.beakya.hellotalk.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ChatActivity.class.getSimpleName();

    private static final int ID_CHAT_CURSOR_LOADER = 1;
    private Button button;
    private EditText contentEditText;
    private RecyclerView chatRecyclerView;
    private String chatTableName = null;
    private ArrayList<String> receiveList;
    private int chatType;
    private String myId;
    private String receiverId;
    String messageContent = null;
    private boolean isCreatedChat = true;
    private boolean isSynchronized = false;
    private ChatAdapter chatAdapter;
    private Socket socket;
    private Uri insertedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString( getString(R.string.user_id), null );

        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);



        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            receiveList = extras.getStringArrayList("receiveList");
            chatType = extras.getInt("chatType");
            chatTableName = extras.getString(TalkContract.ChatRoom.CHAT_LIST_ID);

            if( chatTableName == null ) {
                chatTableName = Utils.sha256(System.currentTimeMillis() + myId);
                isCreatedChat = false;
            }
            if ( chatTableName != null ) {
                ContentResolver resolver = getContentResolver();
                Cursor chatCursor = resolver.query(
                        TalkContract.ChatRoom.CONTENT_URI,
                        new String[] { TalkContract.ChatRoom.CHAT_TYPE, TalkContract.ChatRoom.IS_SYNCHRONIZED },
                        TalkContract.ChatRoom.CHAT_LIST_ID + " = ?", new String[] {chatTableName},
                        null);
                if (chatCursor.getCount() > 0 ) {
                    chatCursor.moveToFirst();
                    isSynchronized = chatCursor.getInt(chatCursor.getColumnIndex(TalkContract.ChatRoom.IS_SYNCHRONIZED)) > 0;
                } else {
                    isCreatedChat = false;
                }
            }

            chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
            chatAdapter = new ChatAdapter(this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            chatRecyclerView.setLayoutManager(linearLayoutManager);
            chatRecyclerView.setAdapter(chatAdapter);
        }


//
//
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageContent = contentEditText.getText().toString();
                if( !(messageContent.length() > 0) ) {
                    Toast.makeText(ChatActivity.this, "빈문자는 보낼수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentResolver resolver = getContentResolver();
                ContentValues chatParams = new ContentValues();
                chatParams.put(TalkContract.ChatRoom.CHAT_LIST_ID, chatTableName);
                chatParams.put(TalkContract.Chat.CREATOR_ID, myId);
                chatParams.put(TalkContract.Chat.MESSAGE_CONTENT, messageContent);
                chatParams.put(TalkContract.Chat.MESSAGE_TYPE, TalkContract.Chat.TYPE_TEXT);
                insertedUri = resolver.insert(TalkContract.Chat.CONTENT_URI, chatParams);
                if( !isCreatedChat ) {
                    Utils.ChatInitialize(ChatActivity.this, chatTableName, chatType, receiveList);
                    isCreatedChat = true;
                }
                int insertedChatRowNumber;
                try {
                    insertedChatRowNumber  = Integer.parseInt(insertedUri.getLastPathSegment());
                } catch ( Exception e ) {
                    throw new RuntimeException("something wrong");
                }
                if( !isSynchronized ) {
                    String message = createMessage(chatTableName, insertedChatRowNumber, messageContent, TalkContract.Chat.TYPE_TEXT, chatType, receiveList);
                    socket.emit( getString(R.string.socket_send_chat), message );
                    Log.d(TAG, "onClick: socket emit with members");
                } else {
                    String message = createMessage(chatTableName, insertedChatRowNumber, messageContent, TalkContract.Chat.TYPE_TEXT, chatType);
                    socket.emit( getString(R.string.socket_send_chat), message );
                    Log.d(TAG, "onClick: socket emit without members");
                }

            }
        });
        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch ( id ) {
            case ID_CHAT_CURSOR_LOADER :
                return new CursorLoader( this, TalkContract.Chat.CONTENT_URI, null, TalkContract.ChatRoom.CHAT_LIST_ID + "=?", new String[] {chatTableName}, null );
            default :
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        chatAdapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        chatAdapter.swapCursor( null );
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        chatAdapter.swapCursor(null);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent<String> event) {
        if( event.getMessage().equals("first_received")) {
            if( !chatTableName.equals(event.getStorage()) ) {
                chatTableName = event.getStorage();
                isCreatedChat = true;
            }
        }
        if ( event.getMessage().equals("message_send_success")) {
            if( insertedUri != null ) {
                ContentValues updateValue = new ContentValues();
                updateValue.put(TalkContract.Chat.IS_SEND, true);
                String segment = insertedUri.getLastPathSegment();
                getContentResolver().update(insertedUri, updateValue, TalkContract.Chat._ID +" = ?", new String[] { segment });
                isSynchronized = true;
//        디버그용 코드 나중에 지울것
//            Cursor cursor = getContentResolver().query(TalkContract.Chat.CONTENT_URI, null, null, null, null);
//            Toast.makeText(ChatActivity.this, "result" + cursor.getCount() , Toast.LENGTH_SHORT).show();
//            while ( cursor.moveToNext() ) {
//                Log.d(TAG, cursor.getInt(cursor.getColumnIndex(TalkContract.Chat._ID)) + " : " +
//                        cursor.getString(cursor.getColumnIndex(TalkContract.Chat.IS_SEND)) + " : " +
//                        cursor.getString(cursor.getColumnIndex(TalkContract.Chat.CREATOR_ID)));
//            }

            }
        }
        getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
    }
    String createMessage (String tableName, int insertedChatRowNumber, String messageContent, String messageType, int chatType, ArrayList<String> receiveList) {
        JSONObject obj = new JSONObject();
        try {
            obj.put( "chatTableName", tableName );
            obj.put( TalkContract.Chat.MESSAGE_CONTENT, messageContent );
            obj.put( TalkContract.Chat.MESSAGE_TYPE, messageType);
            obj.put( TalkContract.Chat.CREATOR_ID, myId );
            obj.put(TalkContract.ChatRoom.CHAT_TYPE, chatType);
            obj.put("members", new JSONArray(receiveList));
            obj.put("insertedChatRowNumber", insertedChatRowNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    String createMessage (String tableName, int insertedChatRowNumber, String messageContent, String messageType, int chatType) {
        JSONObject obj = new JSONObject();
        try {
            obj.put( "chatTableName", tableName );
            obj.put( TalkContract.Chat.MESSAGE_CONTENT, messageContent );
            obj.put( TalkContract.Chat.MESSAGE_TYPE, messageType);
            obj.put( TalkContract.Chat.CREATOR_ID, myId );
            obj.put(TalkContract.ChatRoom.CHAT_TYPE, chatType);
            obj.put("insertedChatRowNumber", insertedChatRowNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
