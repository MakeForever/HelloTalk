package com.beakya.hellotalk.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.adapter.NewChatAdapter;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.HashMapDeserializer;
import com.beakya.hellotalk.utils.HashMapSerializer;
import com.beakya.hellotalk.utils.SimpleDividerItemDecoration;
import com.beakya.hellotalk.utils.Utils;
import com.beakya.hellotalk.viewholder.NewChatViewHolder;
import com.beakya.hellotalk.widget.ChatNameDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class NewChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = NewChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private NewChatAdapter newChatAdapter;
    private Toolbar toolbar;
    private Socket socket;
    private User myInfo;
    private Context mContext;
    private LinearLayout linearLayout;
    private ChatNameDialog chatNameDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        MyApp app = (MyApp) getApplicationContext();
        socket = app.getSocket();
        mContext = this;
        SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
        myInfo = new User(tokenStorage.getString(getString(R.string.user_id), null), tokenStorage.getString(getString(R.string.user_name), null), null);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChatAdapter = new NewChatAdapter();
        mRecyclerView.setAdapter(newChatAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        linearLayout = (LinearLayout) findViewById(R.id.new_chat_linear_layout);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.new_chat_button ) {
            final HashMap<String, User> users = newChatAdapter.getUsers();
            if ( users.size() > 1 ) {
                if( chatNameDialog == null ) {
                    chatNameDialog = new ChatNameDialog(mContext);
                    chatNameDialog.setOnOkListener(new DialogResultListener() {
                        @Override
                        public void onCanceled() {

                        }
                        @Override
                        public void onOKClick(String chatName) {
                            String event = "invite_group_chat";
                            Gson gson = new GsonBuilder()
                                    .registerTypeAdapter(HashMap.class, new HashMapSerializer())
                                    .create();
                            JsonObject object = new JsonObject();

                            final GroupChatRoom chatRoom = new GroupChatRoom(chatName, users, Utils.hashFunction(String.valueOf(System.currentTimeMillis()), "SHA-256"), 2, true);
                            chatRoom.addUser(myInfo);
                            object.addProperty("event", event);
                            object.addProperty("creator", myInfo.getId());
                            object.add("chatRoom", gson.toJsonTree(chatRoom));
                            socket.emit(event, object.toString(), new Ack() {
                                @Override
                                public void call(Object... args) {
                                    Log.d(TAG, "call: ");
                                    Utils.ChatInitialize(NewChatActivity.this, chatRoom);
                                    Intent chatActivityIntent = new Intent(mContext, GroupChatActivity.class);
                                    chatActivityIntent.putExtra("chatRoom", chatRoom);
                                    startActivity(chatActivityIntent);
                                    finish();
                                }
                            });
                        }
                    });
                }
                chatNameDialog.show();
            } else if ( users.size() == 1 ){
                User user = null;
                for ( User t : users.values() ) {
                    user = t;
                }
                int chatType = 1;
                Intent intent = new Intent(mContext , ChatActivity.class );
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
            } else {
                Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.error_new_chat_no_choose_friend), Snackbar.LENGTH_LONG);
                snackbar.setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                snackbar.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_new_chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, TalkContract.User.CONTENT_URI, null, null, null, null);
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
}
