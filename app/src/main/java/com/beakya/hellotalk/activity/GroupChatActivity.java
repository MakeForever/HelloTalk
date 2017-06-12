package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.GroupChatAdapter;
import com.beakya.hellotalk.adapter.PersonalChatAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatAsyncTaskLoader;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.services.ChatService;
import com.beakya.hellotalk.utils.ChatTask;
import com.beakya.hellotalk.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class GroupChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Message>> {
    public static final String TAG = ChatActivity.class.getSimpleName();
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    private static final int ID_CHAT_CURSOR_LOADER = 3;

    private Button button;
    private Button testButton;
    private EditText contentEditText;
    private RecyclerView chatRecyclerView;
    private String myId;
    private String myName;
    String messageContent = null;
    private boolean isStored = true;
    private GroupChatAdapter groupChatAdapter;
    private Socket socket;
    private Context mContext;
    private GroupChatRoom mChatRoom;
    private LinearLayout chatEditTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myId = tokenStorage.getString(getString(R.string.user_id), null);
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
                if (!(messageContent.length() > 0)) {
                    Snackbar.make(chatRecyclerView, "빈문자는 보낼수 없습니다.", Snackbar.LENGTH_SHORT).show();
                    button.setEnabled(true);
                    return;
                }

                String messageId = Utils.hashFunction(myId + chatId + System.currentTimeMillis(), "SHA-1");
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatted = dateFormat.format(calendar.getTime());
                Message message = new Message(messageId, myId, messageContent, chatId, TalkContract.Message.TYPE_TEXT, formatted, 1);
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId);
                String event = getString(R.string.send_group_message);
//                String messageJson = mChatRoom.toJson(message, new User(myId, myName, null), event);
                Gson gson = new GsonBuilder().create();
                JsonElement messageJson = gson.toJsonTree(message);
                JsonObject object = new JsonObject();
                object.addProperty("event", event);
                object.add("message", messageJson);
                groupChatAdapter.addMessage(message);
                socket.emit(event, object.toString());
                Log.d(TAG, "onClick: send_group_message" + messageJson);
                button.setEnabled(true);
            }
        });
        groupChatAdapter = new GroupChatAdapter(this, chatRecyclerView, mChatRoom);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(groupChatAdapter);
        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, this);

        //TODO : 채팅에 들어오면 읽는 emit 하는거 만들기
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra(TalkContract.User.USER_ID, myId);
        intent.putExtra(TalkContract.ChatRooms.CHAT_ID, mChatRoom.getChatId());
        intent.setAction(ChatTask.ACTION_CHANGE_ALL_MESSAGE_READ_STATE);
        startService(intent);
    }

    @Override
    public Loader<ArrayList<Message>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_CHAT_CURSOR_LOADER:
                Log.d(TAG, "onCreateLoader:  new CursorLoader call");
//                return new CursorLoader( this, TalkContract.Message.CONTENT_URI, null, TalkContract.ChatRooms.CHAT_ID + "=?", new String[] {chatId}, null );
                return new ChatAsyncTaskLoader(this, mChatRoom.getChatId());
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }

    }


    @Override
    public void onLoadFinished(Loader<ArrayList<Message>> loader, ArrayList<Message> data) {
        Log.d(TAG, "onLoadFinished: ");
        groupChatAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Message>> loader) {
        Log.d(TAG, "onLoaderReset: ");
        groupChatAdapter.swapCursor(null);
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
//        personalChatAdapter.swapCursor(null);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch (event.getMessage()) {
            case EVENT_NEW_MESSAGE_ARRIVED:
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
                            new String[]{message.getMessageId()});
                    groupChatAdapter.addMessage(message);
                    message.setReadCount(count);
                    mChatRoom.setSynchronized(true);
                    //TODO : 채팅에 들어오면 읽는 emit 하는거 만들기
//                    PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
//                    String t = Utils.createIsReadMessageJsonObj(message.getChatId(), Arrays.asList(new String[] { message.getMessageId()}), chatRoom.getTalkTo());
//                    socket.emit("chat_read", t);
                    JsonObject object = new JsonObject();
                    JsonArray array = new JsonArray();
                    object.addProperty("chatType", mChatRoom.getChatRoomType());
                    object.addProperty(TalkContract.ChatRooms.CHAT_ID, message.getChatId());
                    object.addProperty("from", myId);
                    array.add(message.getMessageId());
                    object.add("messages", array);
                    socket.emit("chat_read", object.toString());
                }
                break;
            case EVENT_SOMEONE_READ_MESSAGE:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
                break;
            default:
                throw new RuntimeException("message not matched");
        }
    }
}
