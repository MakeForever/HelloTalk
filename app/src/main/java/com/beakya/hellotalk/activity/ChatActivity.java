package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.PersonalChatAdapter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    private PersonalChatAdapter personalChatAdapter;
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

                if (!mChatRoom.isSynchronized()) {
                    Utils.ChatInitialize(mContext, mChatRoom);
                    mChatRoom.setSynchronized(true);
                }
                String messageId = Utils.hashFunction(myId + chatId + System.currentTimeMillis(), "SHA-1");

                Message message = null;

                PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                message = new Message(messageId, myId, messageContent, chatId, TalkContract.Message.TYPE_TEXT, Utils.getCurrentTime(), 1);
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId);
                String event = getString(R.string.invite_to_personal_chat);
                String messageJson = chatRoom.toJson(message, new User(myId, myName, null), event);
                socket.emit(event, messageJson);
                Log.d(TAG, "onClick: invite_to_personal_chat" + messageJson);
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, ChatActivity.this);
                button.setEnabled(true);
            }
        });
        PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
        personalChatAdapter = new PersonalChatAdapter(this, chatRoom, chatRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(personalChatAdapter);
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
        personalChatAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Message>> loader) {
        Log.d(TAG, "onLoaderReset: ");
        personalChatAdapter.swapCursor(null);
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
                    personalChatAdapter.addMessage(message);
                    message.setReadCount(count);
                    mChatRoom.setSynchronized(true);
                    String chatReadEvent = "chat_read";
                    PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                    String t = Utils.createIsReadMessageJsonObj(chatReadEvent, message.getChatId(), Arrays.asList(new String[]{message.getMessageId()}), chatRoom.getTalkTo());
                    socket.emit("chat_read", t);
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
