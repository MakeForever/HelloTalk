package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.NewChatAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.HashMapSerializer;
import com.beakya.hellotalk.utils.Serializers;
import com.beakya.hellotalk.utils.SimpleDividerItemDecoration;
import com.beakya.hellotalk.utils.Utils;
import com.beakya.hellotalk.widget.ChatNameDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class NewChatActivity extends ToolBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = NewChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private NewChatAdapter newChatAdapter;
    private Socket socket;
    private User myInfo;
    private Context mContext;
    private LinearLayout linearLayout;
    private ChatNameDialog chatNameDialog = null;
    private int chatType = 0;
    private int originalChatMemberCount = 0;
    private GroupChatRoom chatRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarContentView(R.layout.activity_new_chat);

        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        myInfo = Utils.getMyInfo(mContext);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChatAdapter = new NewChatAdapter();
        mRecyclerView.setAdapter(newChatAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        linearLayout = (LinearLayout) findViewById(R.id.new_chat_linear_layout);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatType = extras.getInt("chatType");
            if ( chatType == ChatRoom.GROUP_CHAT_TYPE ) {
                chatRoom = extras.getParcelable("chatRoom");
                newChatAdapter.setMembers(chatRoom.getUsers().values());
                originalChatMemberCount = chatRoom.getUsers().size();
            } else if (chatType == ChatRoom.PERSONAL_CHAT_TYPE ) {
                ArrayList<User> users = extras.getParcelableArrayList("users");
                newChatAdapter.setMembers(users);
                originalChatMemberCount = users.size();
            }
        }
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.new_chat_button ) {
            final HashMap<String, User> users = newChatAdapter.getUsers();

            int addedMemberCount = users.size() - originalChatMemberCount;
            if ( addedMemberCount < 1 ) {
                Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.error_new_chat_no_choose_friend), Snackbar.LENGTH_LONG);
                snackbar.setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                snackbar.show();
            } else {
                if( chatType == 1 ) {
                    String event = "invite_group_chat";
                    String chatName = chatNameCreateByUsers(users.values());
                    users.put(myInfo.getId(), myInfo);
                    createChatRoomAndSocketEmit(mContext, socket, myInfo, event, users, chatName);
                } else if ( chatType == 2 ) {
                    //TODO addFriend
                    String event = "invite_friend";
                    ArrayList<User> addedUsers = newChatAdapter.getAddedUsers();
                    storeNewFriendsAndSocketEmit(mContext, addedUsers, chatRoom.getChatId(), event, socket, chatRoom);
                } else if ( chatType == 0 ) {
                    if ( addedMemberCount > 1 ) {
                        if( chatNameDialog == null ) {
                            chatNameDialog = new ChatNameDialog(mContext);
                            chatNameDialog.setOnOkListener(new DialogResultListener() {
                                @Override
                                public void onCanceled() {

                                }
                                @Override
                                public void onOKClick(String chatName) {
                                    String event = "invite_group_chat";
                                    users.put(myInfo.getId(), myInfo);
                                    createChatRoomAndSocketEmit(mContext, socket, myInfo, event, users, chatName);
                                }
                            });
                        }
                        chatNameDialog.show();
                    } else {
                        User user = null;
                        for ( User t : users.values() ) {
                            user = t;
                        }
                        int chatType = 1;
                        Intent intent = new Intent(mContext , PersonalChatActivity.class );
                        boolean isSynchronized;
                        boolean isStored;
                        String chatTableName = Utils.ChatTableNameCreator(Arrays.asList(new String [] { myInfo.getId(), user.getId()}));
                        ContentResolver resolver = getContentResolver();
                        Cursor chatCursor = resolver.query(
                                TalkContract.ChatRooms.CONTENT_URI,
                                new String[] { TalkContract.ChatRooms.CHAT_ROOM_TYPE, TalkContract.ChatRooms.IS_SYNCHRONIZED },
                                TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] { chatTableName },
                                null);

                        if (chatCursor.getCount() > 0 ) {
                            chatCursor.moveToFirst();
                            isSynchronized = true;
                            isStored = true;
                        } else {
                            isSynchronized = false;
                            isStored = false;
                        }
                        PersonalChatRoom chatRoom = new PersonalChatRoom( chatTableName, chatType, isSynchronized, user);
                        intent.putExtra("chatRoom", chatRoom);
                        intent.putExtra("is_stored", isStored);
                        startActivity(intent);
                        finish();
                    }
                }
            }

        } else if ( item.getItemId() == android.R.id.home ) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_new_chat_menu, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.IS_MY_FRIEND +" = ? ",
                new String[] { "1" },
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        newChatAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        newChatAdapter.swapCursor(null);
    }

    public interface DialogResultListener {
        public void onCanceled();
        public void onOKClick(String chatName);
    }
    String chatNameCreateByUsers(Collection<User> users ) {
        int count = 0;
        StringBuilder builder = new StringBuilder();
        for ( User user : users ) {
            count++;
            builder.append(user.getName());
            if( users.size() > 3 ) {
                if ( count < 3 ) {
                    builder.append(", ");
                } else
                    break;
            } else {
                if ( count < users.size() ) {
                    builder.append(", ");
                } else
                    break;
            }
        }
        if( users.size() - count > 0 ) {
            builder.append("외 " + (users.size() - count) +"명" );
        }
        builder.append("의 대화방");
        return builder.toString();
    }
    public void createChatRoomAndSocketEmit (final Context context,
                                             Socket socket, User myInfo, String event, final HashMap<String, User> users, String chatName ) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(HashMap.class, new HashMapSerializer())
                .create();
        JsonObject object = new JsonObject();
        final GroupChatRoom chatRoom = new GroupChatRoom(
                chatName,
                users,
                Utils.hashFunction(String.valueOf(System.currentTimeMillis()), "SHA-256"),
                ChatRoom.GROUP_CHAT_TYPE,
                true
        );
        object.addProperty("event", event);
        object.addProperty("sender", myInfo.getId());
        object.add("chatRoom", gson.toJsonTree(chatRoom));
        socket.emit(event, object.toString(), new Ack() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: ");
                Utils.ChatInitialize(NewChatActivity.this, chatRoom);
                Intent chatActivityIntent = new Intent(context, GroupChatActivity.class);
                chatActivityIntent.putExtra("chatRoom", chatRoom);
                startActivity(chatActivityIntent);
                finish();
            }
        });
    }

    public void storeNewFriendsAndSocketEmit(final Context context,
                                             final ArrayList<User> users, final String chatId, String event, final Socket socket, final GroupChatRoom chatRoom ) {

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserSerializer())
                .registerTypeAdapter(HashMap.class, new HashMapSerializer())
                .create();
        JsonArray usersElement = null;
        try {

            usersElement = gson.toJsonTree(users, new TypeToken<ArrayList<User>>() {}.getType()).getAsJsonArray();
        } catch (Exception e ) {
            e.printStackTrace();
        }
        chatRoom.addUser(myInfo);
        final JsonObject object = new JsonObject();
        object.add("users", usersElement);
        object.addProperty(TalkContract.ChatRooms.CHAT_ID, chatId);
        object.addProperty("event", event);
        object.addProperty("sender", myInfo.getId());
        object.add("chatRoom", gson.toJsonTree(chatRoom));
        socket.emit(event, object.toString(), new Ack() {
            @Override
            public void call(Object... args) {
                Utils.insertChatMembers(context.getContentResolver(), chatId, users);
                String event = getString(R.string.send_group_message);
                for ( User user : users ) {
                    String messageId = Utils.hashFunction(myInfo.getId() + chatId + System.currentTimeMillis(), "SHA-1");
                    Message message = new Message(messageId, "system", myInfo.getName() + "님이 " + user.getName() +"님을 초대했습니다.", chatId, TalkContract.Message.TYPE_TEXT,Utils.getCurrentTime(), false, 0);
                    Utils.insertMessage(context, message, chatId, true);
//                    JsonObject object1 = new JsonObject();
//                    object1.addProperty("event", event);
//                    object1.add("message",gson.toJsonTree(message));
//                    socket.emit(event, object1.toString());
                }
                Intent intent = new Intent();
                intent.putExtra("users", users);
                setResult(100, intent);
                finish();
            }
        });
    }
}
