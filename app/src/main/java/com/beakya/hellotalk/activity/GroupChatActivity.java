package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.beakya.hellotalk.adapter.GroupChatAdapter;
import com.beakya.hellotalk.adapter.MemberListAdapter;
import com.beakya.hellotalk.asynctaskloader.ChatAsyncTaskLoader;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.GroupChatReadEventInfo;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PayLoad;
import com.beakya.hellotalk.objs.SocketJob;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.SocketEmitFunctions;
import com.beakya.hellotalk.utils.SocketUtil;
import com.beakya.hellotalk.utils.TaskRunner;
import com.beakya.hellotalk.utils.Utils;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class GroupChatActivity extends ChatActivity  {
    public static final String TAG = PersonalChatActivity.class.getSimpleName();
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    public static final String EVENT_INVITED_USER = "event_invited_user";
    public static final int ADD_FRIEND_REQUEST_CODE = 10;
    private static final int ID_CHAT_CURSOR_LOADER = 3;
    private Button button;
    private EditText contentEditText;
    private RecyclerView memberRecyclerView;
    private RecyclerView chatRecyclerView;
    private Button addFriendButton;
    private User myInfo;
    String messageContent = null;
    private boolean isStored = true;
    private GroupChatAdapter groupChatAdapter;
    private MemberListAdapter memberListAdapter;
    private Socket socket;
    private Context mContext;
    private GroupChatRoom mChatRoom;
    private LinearLayout chatEditTextView;
    private DrawerLayout mDrawer;
    private boolean isMessageUpdated = false;
    LoaderManager.LoaderCallbacks<ArrayList<Message>> messageLoaderCallBacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_chat);
        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        myInfo = Utils.getMyInfo(mContext);
        button = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        chatEditTextView = (LinearLayout) findViewById(R.id.chat_edit_text_layout);
        addFriendButton = (Button) findViewById(R.id.chat_add_friend_button);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mChatRoom = extras.getParcelable("chatRoom");
            isStored = extras.getBoolean("is_stored");
            mChatRoom.addUser(myInfo);
        }
        // ToolBar setup

        super.setToolbar(mChatRoom.getChatName());

        mDrawer = (DrawerLayout) findViewById(R.id.chat_drawer_layout);
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
                String messageId = Utils.hashFunction(myInfo.getId() + chatId + System.currentTimeMillis(), "SHA-1");

                Message message = new Message(messageId, myInfo.getId(), messageContent, chatId, TalkContract.Message.TYPE_TEXT, Utils.getCurrentTime(), false, mChatRoom.getUsers().size() - 1);
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId, false);
                String event = getString(R.string.send_group_message);
//                String messageJson = mChatRoom.toJson(stringMessage, new User(myId, myName, null), event);
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

        memberRecyclerView = (RecyclerView) findViewById(R.id.member_recycler_view);;
        memberListAdapter = new MemberListAdapter();
        memberRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        memberRecyclerView.setAdapter(memberListAdapter);
        ArrayList<User> users = new ArrayList<>(mChatRoom.getUsers().values());
        List<User> newUsers = new ArrayList<>();
        for ( User user : users ) {
            if ( user.isMember()) newUsers.add(user);
        }
        memberListAdapter.swapData(newUsers);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewChatActivity.class);
                intent.putExtra("chatType", mChatRoom.getChatRoomType());
                intent.putExtra("chatRoom", mChatRoom);
                startActivityForResult(intent, ADD_FRIEND_REQUEST_CODE);
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                }
            }
        });

        messageLoaderCallBacks = new LoaderManager.LoaderCallbacks<ArrayList<Message>>() {
            @Override
            public Loader<ArrayList<Message>> onCreateLoader(int id, Bundle args) {
                switch (id) {
                    case ID_CHAT_CURSOR_LOADER:
                        return new ChatAsyncTaskLoader(mContext, mChatRoom.getChatId());
                    default:
                        throw new RuntimeException("asyncTaskLoader id not matched");
                }
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Message>> loader, ArrayList<Message> data) {
                groupChatAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Message>> loader) {
                groupChatAdapter.swapCursor(null);
            }
        };
        getSupportLoaderManager().initLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == ADD_FRIEND_REQUEST_CODE && resultCode == 100 ) {
            ArrayList<User> users = data.getParcelableArrayListExtra("users");
            memberListAdapter.addMember(users);
            for ( User user : users )
                mChatRoom.addUser(user);
            getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
//        if ( !isMessageUpdated ) {
//            isMessageUpdated = true;
//            Intent intent = new Intent(this, ChatService.class);
//            intent.putExtra(TalkContract.User.USER_ID, myInfo.getId());
//            intent.putExtra("chatType", mChatRoom.getChatRoomType());
//            intent.putExtra(TalkContract.ChatRooms.CHAT_ID, mChatRoom.getChatId());
//            intent.setAction(ChatTask.ACTION_CHANGE_ALL_MESSAGE_READ_STATE);
//            startService(intent);
//            isMessageUpdated = false;
//        }

        TaskRunner runner = TaskRunner.getInstance();
        PayLoad<SocketEmitFunctions.bFunction> payLoad = new PayLoad<>(
                SocketUtil.handleNotReadMessageFunction(
                        new GroupChatReadEventInfo(
                                myInfo,
                                mChatRoom.getChatId(),
                                mChatRoom.getChatRoomType()
                        ),
                        this
                )

        );
        runner.addJob(new SocketJob("chat_read", payLoad, this));
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
//        personalChatAdapter.swapCursor(null);
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.image_setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item != null && item.getItemId() == R.id.image_action_next ) {
            if( mDrawer.isDrawerOpen(Gravity.RIGHT) ) {
                mDrawer.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawer.openDrawer(Gravity.RIGHT);
            }
        } else if ( item.getItemId() == android.R.id.home ) {
            finish();
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.UserInviteEvent event ) {
        if ( event.getMessage().equals( EVENT_INVITED_USER ) ) {
            ArrayList<User> users = event.getStorage();
            memberListAdapter.addMember(users);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch (event.getMessage()) {
            case EVENT_NEW_MESSAGE_ARRIVED:
                Log.d(TAG, "onMessageEvent: new message arrived");
                Message message = event.getStorage();
                if (message.getChatId().equals(mChatRoom.getChatId())) {
                    int count = message.getReadCount();
                    --count;
                    ContentValues values = new ContentValues();
                    values.put(TalkContract.Message.IS_READ, 1);
                    values.put(TalkContract.Message.READING_COUNT, count);
                    ContentResolver resolver = getContentResolver();
                    resolver.update(
                            TalkContract.Message.CONTENT_URI,
                            values,
                            TalkContract.Message.MESSAGE_ID + " = ?",
                            new String[]{message.getMessageId()});
                    groupChatAdapter.addMessage(message);
                    message.setReadCount(count);
                    mChatRoom.setSynchronized(true);
                    String emitParam = Utils.groupChatReadObjCreator(mChatRoom.getChatRoomType(), myInfo, mChatRoom.getChatId(), Arrays.asList(new String[] { message.getMessageId() }) );
                    socket.emit("chat_read", emitParam);
                }
                break;
            case EVENT_SOMEONE_READ_MESSAGE:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
                break;
            case EVENT_INVITED_USER:
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
                break;
            default:
                throw new RuntimeException("stringMessage not matched");
        }
    }
}
