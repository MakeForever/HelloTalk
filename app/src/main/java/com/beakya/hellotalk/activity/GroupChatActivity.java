package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.beakya.hellotalk.utils.JsonUtils;
import com.beakya.hellotalk.utils.Logger;
import com.beakya.hellotalk.utils.SocketEmitFunctions;
import com.beakya.hellotalk.utils.SocketUtil;
import com.beakya.hellotalk.utils.TaskRunner;
import com.beakya.hellotalk.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 6. 7..
 */

public class GroupChatActivity extends ChatActivity  {
    public static final String TAG = GroupChatActivity.class.getSimpleName();
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    public static final String EVENT_INVITED_USER = "event_invited_user";
    public static final String EVENT_SOME_ONE_LEAVE_ROOM = "evnet_someone_leave_room";
    public static final int ADD_FRIEND_REQUEST_CODE = 10;
    private static final int ID_CHAT_CURSOR_LOADER = 3;
    private Button sendButton;
    private EditText contentEditText;
    private RecyclerView memberRecyclerView;
    private RecyclerView chatRecyclerView;
    private Button addFriendButton;
    private User myInfo;
    String messageContent = null;
    private boolean isStored = true;
    private GroupChatAdapter groupChatAdapter;
    private MemberListAdapter memberListAdapter;
    private Socket mSocket;
    private Context mContext;
    private GroupChatRoom mChatRoom;
    private LinearLayout chatEditTextView;
    private DrawerLayout mDrawer;
    private Button chatLeaveButton;
    private boolean isMessageUpdated = false;
    LoaderManager.LoaderCallbacks<ArrayList<Message>> messageLoaderCallBacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_chat);
        MyApp app = (MyApp) getApplicationContext();
        mSocket = app.getSocket();
        mContext = this;
        myInfo = Utils.getMyInfo(mContext);
        sendButton = (Button) findViewById(R.id.chat_send_button);
        contentEditText = (EditText) findViewById(R.id.chat_content_edit_text);
        chatRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        chatEditTextView = (LinearLayout) findViewById(R.id.chat_edit_text_layout);
        addFriendButton = (Button) findViewById(R.id.chat_add_friend_button);
        chatLeaveButton = (Button) findViewById(R.id.button_for_chat_leave);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mChatRoom = extras.getParcelable("chatRoom");
            isStored = extras.getBoolean("is_stored");
            mChatRoom.addUser(myInfo);
        }
        // ToolBar setup

        super.setToolbar(mChatRoom.getChatName());
        mDrawer = (DrawerLayout) findViewById(R.id.chat_drawer_layout);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButton.setEnabled(false);
                String chatId = mChatRoom.getChatId();
                int chatType = mChatRoom.getChatRoomType();
                messageContent = contentEditText.getText().toString();
                if (!(messageContent.length() > 0)) {
                    Snackbar.make(chatRecyclerView, "빈문자는 보낼수 없습니다.", Snackbar.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                    return;
                }
                String messageId = Utils.hashFunction(myInfo.getId() + chatId + System.currentTimeMillis(), "SHA-1");

                Message message = new Message(messageId, myInfo.getId(), messageContent, chatId, TalkContract.Message.TYPE_TEXT, Utils.getCurrentTime(), false, mChatRoom.getMembersSize() - 1);
                int insertedChatRowNumber = Utils.insertMessage(mContext, message, chatId, false);
                String event = getString(R.string.send_group_message);
//                String messageJson = mChatRoom.toJson(stringMessage, new User(myId, myName, null), event);
                Gson gson = new GsonBuilder().create();
                JsonElement messageJson = gson.toJsonTree(message);
                JsonObject object = new JsonObject();
                object.addProperty("event", event);
                object.add("message", messageJson);
                getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
                groupChatAdapter.addMessage(message);
                mSocket.emit(event, object.toString());
                sendButton.setEnabled(true);
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
        memberListAdapter.swapData(mChatRoom.getMembers());
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
        chatLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("채팅방 나가기");
                builder.setMessage("이 채팅방을 나가시겠습니까?");
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //socketThing
                        JsonObject object = JsonUtils.makeLeaveRoomObj(
                                mContext,
                                mChatRoom.getChatId(),
                                mChatRoom.getChatRoomType(),
                                Utils.getMyInfo(mContext).getId()
                        );
                        mSocket.emit("someone_leave_chat_room", object.toString());
                        Utils.deleteChatRoom(mContext, mChatRoom.getChatId());
                        finish();
                    }
                });
                builder.show();
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
        TaskRunner runner = TaskRunner.getInstance();
        PayLoad<SocketEmitFunctions.bFunction> payLoad = new PayLoad<>(
                SocketUtil.checkNotReadMessages(
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
    public String getCurrentChatId() {
        return mChatRoom.getChatId();
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
    public void onMessageEvent(Events.UpdateEvent event ) {
        if ( event.getMessage().equals( EVENT_USER_CHANGE_PROFILE_IMG ) ) {
            memberListAdapter.swapData(mChatRoom.getMembers());
            groupChatAdapter.updateAllViewHolders();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.UserInviteEvent event ) {
        if ( event.getMessage().equals( EVENT_INVITED_USER ) ) {
            ArrayList<User> users = event.getStorage();
            for ( User user : users ) {
                mChatRoom.addUser(user);
            }
            memberListAdapter.swapData(mChatRoom.getMembers());
            getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.UserLeaveEvent event ) {
        if ( event.getMessage().equals( EVENT_SOME_ONE_LEAVE_ROOM ) ) {
            String userId = event.getStorage();
            for ( User user : mChatRoom.getUsers().values() ) {
                if ( user.getId().equals(userId) ) {
                    user.setToMember(false);
                }
            }
            memberListAdapter.deleteMember(userId);
            getSupportLoaderManager().restartLoader(ID_CHAT_CURSOR_LOADER, null, messageLoaderCallBacks);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.MessageEvent event) {
        switch (event.getMessage()) {
            case EVENT_NEW_MESSAGE_ARRIVED:
                Logger.d(TAG, "new message arrived");
                Message message = event.getStorage();
                if (message.getChatId().equals(mChatRoom.getChatId())) {
//                    int count = message.getReadCount();
//                    --count;
//                    ContentValues values = new ContentValues();
//                    values.put(TalkContract.Message.IS_READ, 1);
//                    values.put(TalkContract.Message.READING_COUNT, count);
//                    ContentResolver resolver = getContentResolver();
//                    resolver.update(
//                            TalkContract.Message.CONTENT_URI,
//                            values,
//                            TalkContract.Message.MESSAGE_ID + " = ?",
//                            new String[]{message.getMessageId()});
//                    groupChatAdapter.addMessage(message);
//                    message.setReadCount(count);
//                    mChatRoom.setSynchronized(true);
//                    String emitParam = Utils.groupChatReadObjCreator(mChatRoom.getChatRoomType(), myInfo, mChatRoom.getChatId(), Arrays.asList(new String[] { message.getMessageId() }) );
//                    mSocket.emit("chat_read", emitParam);
                    TaskRunner runner = TaskRunner.getInstance();
                    PayLoad<SocketEmitFunctions.bFunction> payLoad = new PayLoad<>(
                            SocketUtil.checkNotReadMessages(
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
