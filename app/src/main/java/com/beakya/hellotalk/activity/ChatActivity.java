package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.ChatAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatAsyncTaskLoader;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.services.ChatService;
import com.beakya.hellotalk.utils.ChatTask;
import com.beakya.hellotalk.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import io.socket.client.Socket;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Message>> {
    public static final String TAG = ChatActivity.class.getSimpleName();
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    private static final int ID_CHAT_CURSOR_LOADER = 1;

    private Button button;
    private Button testButton;
    private EditText contentEditText;
    private RecyclerView chatRecyclerView;
    private String myId;
    private String myName;
    String messageContent = null;
    private boolean isStored = true;
    private ChatAdapter chatAdapter;
    private Socket socket;
    private Context mContext;
    private ChatRoom mChatRoom;
    private LinearLayout chatEditTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString( getString(R.string.user_id), null );
        myName = tokenStorage.getString(getString(R.string.user_name), null);
        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        chatEditTextView = (LinearLayout) findViewById(R.id.chat_edit_text_layout);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mChatRoom = extras.getParcelable("chatRoom");
            isStored = extras.getBoolean("is_stored");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                String chatId = mChatRoom.getChatId();
                int chatType = mChatRoom.getChatRoomType();
                messageContent = contentEditText.getText().toString();
                if( !(messageContent.length() > 0) ) {
//                    Toast.makeText(ChatActivity.this, "빈문자는 보낼수 없습니다.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(chatRecyclerView, "빈문자는 보낼수 없습니다.", Snackbar.LENGTH_SHORT).show();

                    button.setEnabled(true);
                    return;
                }

                if( !mChatRoom.isSynchronized() ) {
                    Utils.ChatInitialize(mContext, mChatRoom);
                    mChatRoom.setSynchronized(true);
                }
                String messageId =  Utils.hashFunction( myId + chatId + System.currentTimeMillis(), "SHA-1" );
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatted = dateFormat.format(calendar.getTime());
                Message message = null;
                if ( chatType == 1 ) {
                    PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                    message = new Message(messageId, myId, messageContent, chatId, TalkContract.Message.TYPE_TEXT, formatted  ,1);
                    int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId);
                    String messageJson = chatRoom.toExportedJson(message, new User(myId, myName, null));
//                    message = new Message(messageId, myId, String.valueOf(i), chatId, TalkContract.Message.TYPE_TEXT, formatted  ,1);

//                    String messageJson = createPersonalMessage(chatRoom, message, chatRoom.getTalkTo());


                    socket.emit(getString(R.string.invite_to_personal_chat), messageJson );
                    Log.d(TAG, "onClick: invite_to_personal_chat" + messageJson);


                } else {
                    //group chat
                }
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, ChatActivity.this);
                //TODO : // message에 모든 칼럼이 다 들어가야 된다. created_time bind할때 parse 에러 발생 고칠것
                button.setEnabled(true);
            }
        });
        PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
        chatAdapter = new ChatAdapter(this , chatRoom, chatRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, this);

        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra(TalkContract.User.USER_ID, myId);
        intent.putExtra("user", chatRoom.getTalkTo());
        intent.putExtra(TalkContract.ChatRooms.CHAT_ID, mChatRoom.getChatId());
        intent.setAction(ChatTask.ACTION_CHANGE_ALL_MESSAGE_READ_STATE);
        startService(intent);
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



    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
//        chatAdapter.swapCursor(null);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch ( event.getMessage() ) {
            case EVENT_NEW_MESSAGE_ARRIVED :
                Message message = event.getStorage();
                if (message.getChatId().equals(mChatRoom.getChatId())) {
                    int count = message.isReadCount();
                    ContentValues values = new ContentValues();
                    values.put(TalkContract.Message.READING_COUNT, --count);
                    ContentResolver resolver = getContentResolver();
                    resolver.update(
                            TalkContract.Message.CONTENT_URI,
                            values,
                            TalkContract.Message.MESSAGE_ID + " = ?",
                            new String[ ] { message.getMessageId() });
                    chatAdapter.addMessage(message);
                    message.setReadCount(count);
                    mChatRoom.setSynchronized( true );

                    PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                    String t = Utils.createIsReadMessageJsonObj(message.getChatId(), Arrays.asList(new String[] { message.getMessageId()}), chatRoom.getTalkTo());
                    socket.emit("chat_read", t);
                }
                break;
            case EVENT_SOMEONE_READ_MESSAGE:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
                break;
            default :
                throw new RuntimeException("message not matched");
        }
    }

    String createMessage (String tableName, int insertedChatRowNumber, String messageId, String messageContent, int messageType, int chatType, HashMap<String, User> receiveList, int readCount) {
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
            chatObj.put(TalkContract.Message.READING_COUNT, readCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatObj.toString();
    }

    String createPersonalMessage (PersonalChatRoom groupChatRoom, Message message, User user ) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("message", createMessageJsonObject(message));
            obj.put("chat_room", createPersonalChatRoomJsonObject(groupChatRoom));
            obj.put("receive", createUserObject(user.getId(), user.getName()));
            obj.put("from", createUserObject(myId, myName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
    JSONObject createUserObject( String id, String name ) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(TalkContract.User.USER_NAME, name );
        object.put(TalkContract.User.USER_ID, id);
        return object;
    }
    JSONObject createPersonalChatRoomJsonObject(PersonalChatRoom groupChatRoom) {
        JSONObject object = new JSONObject();
        try {
            object.put(TalkContract.ChatRooms.CHAT_ID, groupChatRoom.getChatId());
            object.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, groupChatRoom.getChatRoomType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
    JSONObject createMessageJsonObject ( Message message ) {
        JSONObject object = new JSONObject();
        try {
            object.put(TalkContract.ChatRooms.CHAT_ID, message.getChatId());
            object.put(TalkContract.Message.MESSAGE_ID, message.getMessageId());
            object.put(TalkContract.Message.CREATOR_ID, message.getCreatorId());
            object.put(TalkContract.Message.MESSAGE_CONTENT, message.getMessageContent());
            object.put(TalkContract.Message.MESSAGE_TYPE, message.getMessageType());
            object.put(TalkContract.Message.READING_COUNT, message.isReadCount());
            object.put(TalkContract.Message.CREATED_TIME, message.getCreatedTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

}
