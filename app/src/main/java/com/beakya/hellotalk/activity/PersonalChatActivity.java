package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.MemberListAdapter;
import com.beakya.hellotalk.adapter.PersonalChatAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatAsyncTaskLoader;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.services.ChatService;
import com.beakya.hellotalk.utils.ChatTask;
import com.beakya.hellotalk.utils.Serializers;
import com.beakya.hellotalk.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.Socket;


public class PersonalChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Message>> {
    public static final String TAG = PersonalChatActivity.class.getSimpleName();
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    public static final String EVENT_INVITED_USER = "event_invited_user";
    private static final int ID_CHAT_CURSOR_LOADER = 1;

    private Button chatSendButton;
    private EditText contentEditText;
    private RecyclerView chatRecyclerView;
    private RecyclerView memberRecyclerView;
    private MemberListAdapter memberListAdapter;
    private User myInfo;
    String messageContent = null;
    private boolean isStored = true;
    private Button addFriendButton;
    private PersonalChatAdapter personalChatAdapter;
    private Socket socket;
    private Context mContext;
    private PersonalChatRoom mChatRoom;
    private LinearLayout chatEditTextView;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myInfo = Utils.getMyInfo(mContext);
        chatSendButton = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        chatEditTextView = (LinearLayout) findViewById(R.id.chat_edit_text_layout);
        addFriendButton = (Button) findViewById(R.id.chat_add_friend_button);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mChatRoom = extras.getParcelable("chatRoom");
            isStored = extras.getBoolean("is_stored");
        }

        // ToolBar setup
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.chat_drawer_layout);
        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatSendButton.setEnabled(false);
                String chatId = mChatRoom.getChatId();
                int chatType = mChatRoom.getChatRoomType();
                messageContent = contentEditText.getText().toString();
                if (!(messageContent.length() > 0)) {
                    Snackbar.make(chatRecyclerView, "빈문자는 보낼수 없습니다.", Snackbar.LENGTH_SHORT).show();

                    chatSendButton.setEnabled(true);
                    return;
                }

                if (!mChatRoom.isSynchronized()) {
                    Utils.ChatInitialize(mContext, mChatRoom);
                    mChatRoom.setSynchronized(true);
                }
                String messageId = Utils.hashFunction(myInfo.getId() + chatId + System.currentTimeMillis(), "SHA-1");
                PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                Message message = new Message(messageId, myInfo.getId(), messageContent, chatId, TalkContract.Message.TYPE_TEXT, Utils.getCurrentTime(), 1);
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId);
                String event = getString(R.string.invite_to_personal_chat);
//                String messageJson = chatRoom.toJson(message, new User(myId, myName, null), event);event
                String messageString = createMessageJson( mChatRoom, message, Utils.getMyInfo(mContext) );
                socket.emit( event, messageString );
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, PersonalChatActivity.this);
                chatSendButton.setEnabled(true);
            }
        });
        PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
        personalChatAdapter = new PersonalChatAdapter(this, chatRoom, chatRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(personalChatAdapter);
        memberRecyclerView = (RecyclerView) findViewById(R.id.member_recycler_view);;
        memberListAdapter = new MemberListAdapter();
        memberRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        memberRecyclerView.setAdapter(memberListAdapter);
        memberListAdapter.swapData(Arrays.asList(new User[] { mChatRoom.getTalkTo(), Utils.getMyInfo(mContext)}));

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewChatActivity.class);
                ArrayList<User> users = new ArrayList<>();
                users.add(mChatRoom.getTalkTo());
                intent.putExtra("chatType", mChatRoom.getChatRoomType());
                intent.putParcelableArrayListExtra("users", users);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, this);
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra(TalkContract.User.USER_ID, myInfo.getId());
        intent.putExtra("receiver", chatRoom.getTalkTo());
        intent.putExtra("chatType", chatRoom.getChatRoomType());
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
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item != null && item.getItemId() == R.id.image_action_next ) {
            if( mDrawer.isDrawerOpen(Gravity.RIGHT) ) {
                mDrawer.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawer.openDrawer(Gravity.RIGHT);
            }
        }
        return false;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.image_setting_menu, menu);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch (event.getMessage()) {
            case EVENT_NEW_MESSAGE_ARRIVED:
                Message message = event.getStorage();
                if (message.getChatId().equals(mChatRoom.getChatId())) {
                    PersonalChatRoom chatRoom = (PersonalChatRoom) mChatRoom;
                    String t = Utils.personalChatReadObjCreator(
                            chatRoom.getChatRoomType(),
                            Utils.getMyInfo(mContext),
                            chatRoom.getTalkTo(),
                            chatRoom.getChatId(),
                            new ArrayList<String>(Arrays.asList(new String[] { message.getMessageId()}))
                    );
                    socket.emit("chat_read", t);
                    int count = message.isReadCount();
                    ContentValues values = new ContentValues();
                    values.put(TalkContract.Message.READING_COUNT, --count);
                    ContentResolver resolver = getContentResolver();
                    resolver.update(
                            TalkContract.Message.CONTENT_URI,
                            values,
                            TalkContract.Message.MESSAGE_ID + " = ?",
                            new String[]{message.getMessageId()});
                    message.setReadCount(count);
                    mChatRoom.setSynchronized(true);
                    personalChatAdapter.addMessage(message);
                }
                break;
            case EVENT_SOMEONE_READ_MESSAGE:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
                break;
            case EVENT_INVITED_USER:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, this);
                break;
            default:
                throw new RuntimeException("message not matched");
        }
    }
    String createMessageJson (PersonalChatRoom personalChatRoom, Message message, User myInfo ) {
        JsonObject result = new JsonObject();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserSerializer())
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .create();
        JsonObject chatRoomElement = gson.toJsonTree(personalChatRoom).getAsJsonObject();
        JsonElement messageElement = gson.toJsonTree(message);
        JsonElement userElement = gson.toJsonTree(myInfo);
        JsonElement targetElement = chatRoomElement.get("talkTo");
        chatRoomElement.add("talkTo", userElement);
        result.add("chatRoom", chatRoomElement);
        result.add("message", messageElement);
        result.add("receiver", targetElement);
        return result.toString();
    }

}
