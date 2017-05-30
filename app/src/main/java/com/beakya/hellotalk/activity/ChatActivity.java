package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.LoaderManager;
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
import com.beakya.hellotalk.asynctaskloader.ChatAsyncTaskLoader;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.events.MessageEvent;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.Socket;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Message>> {
    public static final String TAG = ChatActivity.class.getSimpleName();

    private static final int ID_CHAT_CURSOR_LOADER = 1;



    private Button button;
    private EditText contentEditText;
    private RecyclerView chatRecyclerView;
    private String myId;
    private String receiverId;
    String messageContent = null;
    private boolean isStored = true;
    private boolean isSynchronized = false;
    private ChatAdapter chatAdapter;
    private Socket socket;
    private Context mContext;
    private ChatRoom mChatRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString( getString(R.string.user_id), null );
        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mChatRoom = extras.getParcelable("chatRoom");
            isStored = extras.getBoolean("is_stored");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatId = mChatRoom.getChatId();
                int chatType = mChatRoom.getChatRoomType();
                messageContent = contentEditText.getText().toString();
                if( !(messageContent.length() > 0) ) {
                    Toast.makeText(ChatActivity.this, "빈문자는 보낼수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if( !isStored ) {
                    Utils.ChatInitialize(mContext, mChatRoom);
                    isStored = true;
                }
                String messageId =  Utils.hashFunction( myId + chatId + System.currentTimeMillis(), "SHA-1" );
                Message message = new Message(messageId, myId, messageContent, chatId, TalkContract.Message.TYPE_TEXT, mChatRoom.getMembersCount());
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId);

                if( !isSynchronized ) {
                    String messageJson = createMessage(chatId, insertedChatRowNumber, messageId, messageContent, TalkContract.Message.TYPE_TEXT, chatType, mChatRoom.getUserList());
                    socket.emit(getString(R.string.invite_to_chat), messageJson );
                    Log.d(TAG, "onClick: socket emit with members");
                } else {
                    String messageJson = createMessage(chatId, insertedChatRowNumber, messageContent, TalkContract.Message.TYPE_TEXT, chatType);
                    socket.emit( getString(R.string.socket_send_chat), message );
                    Log.d(TAG, "onClick: socket emit without members");
                }
                chatAdapter.addMessage(message);
            }
        });

        chatAdapter = new ChatAdapter(this , mChatRoom, chatRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, this);
    }

    @Override
    public Loader<ArrayList<Message>> onCreateLoader(int id, Bundle args) {
        switch ( id ) {
            case ID_CHAT_CURSOR_LOADER :
                Log.d(TAG, "onCreateLoader:  new CursorLoader call" );
//                return new CursorLoader( this, TalkContract.Message.CONTENT_URI, null, TalkContract.ChatRooms.CHAT_ID + "=?", new String[] {chatId}, null );
                return new ChatAsyncTaskLoader(this, mChatRoom.getChatId());
            default :
                throw new RuntimeException("Loader Not Implemented: " + id);
        }

    }


    @Override
    public void onLoadFinished(Loader<ArrayList<Message>> loader, ArrayList<Message> data) {
        Log.d(TAG, "onLoadFinished: ");
        chatAdapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Message>> loader) {
        Log.d(TAG, "onLoaderReset: ");
        chatAdapter.swapCursor( null );
    }



//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        Log.d(TAG, "onLoadFinished: ");
//        chatAdapter.swapCursor( data );
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        Log.d(TAG, "onLoaderReset: ");
//        chatAdapter.swapCursor( null );
//    }


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
//        if( event.getMessage().equals("first_received")) {
//            if( !chatId.equals(event.getStorage()) ) {
//                chatId = event.getStorage();
//                isStored = true;
//            }
//        }
//        if ( event.getMessage().equals("message_send_success")) {
//            if( insertedUri != null ) {
//                ContentValues updateValue = new ContentValues();
//                updateValue.put(TalkContract.Message.IS_SEND, true);
//                String segment = insertedUri.getLastPathSegment();
//                getContentResolver().update(insertedUri, updateValue, TalkContract.Message._ID +" = ?", new String[] { segment });
//                isSynchronized = true;
//
//            }
//        }
        getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
    }
    String createMessage (String tableName, int insertedChatRowNumber, String messageId, String messageContent, int messageType, int chatType, HashMap<String, User> receiveList) {
        JSONObject chatObj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for( User user : receiveList.values() ) {
                JSONObject object = new JSONObject();
                object.put(TalkContract.User.USER_ID, user.getId());
                object.put(TalkContract.User.USER_NAME, user.getName());
                array.put(object);
            }
            chatObj.put(TalkContract.ChatRooms.CHAT_ID, tableName );
            chatObj.put( TalkContract.Message.MESSAGE_CONTENT, messageContent );
            chatObj.put( TalkContract.Message.MESSAGE_TYPE, messageType);
            chatObj.put( TalkContract.Message.CREATOR_ID, myId );
            chatObj.put(TalkContract.Message.MESSAGE_ID, messageId);
            chatObj.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, chatType);
            chatObj.put("members", array);
            chatObj.put("insertedChatRowNumber", insertedChatRowNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatObj.toString();
    }

    String createMessage (String tableName, int insertedChatRowNumber, String messageContent, int messageType, int chatType) {
        JSONObject obj = new JSONObject();
        try {
            obj.put( "chatId", tableName );
            obj.put( TalkContract.Message.MESSAGE_CONTENT, messageContent );
            obj.put( TalkContract.Message.MESSAGE_TYPE, messageType);
            obj.put( TalkContract.Message.CREATOR_ID, myId );
            obj.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, chatType);
            obj.put("insertedChatRowNumber", insertedChatRowNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
