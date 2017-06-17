package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.socket.client.Ack;
import io.socket.client.Socket;

import static com.beakya.hellotalk.activity.PersonalChatActivity.EVENT_NEW_MESSAGE_ARRIVED;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatTask {
    public static final String TAG = ChatTask.class.getSimpleName();
    public static final String ACTION_STORAGE_PERSONAL_CHAT_DATA = "action_storage_chat_data";
    public static final String ACTION_STORAGE_GROUP_CHAT_DATA = "action_storage_group_chat_data";
    public static final String ACTION_HANDLE_INVITE_RESULT = "action_handle_invite_result";
    public static final String ACTION_HANDLE_READ_CHAT = "action_handle_read_chat";
    public static final String ACTION_STORAGE_GROUP_CHAT_INVITE = "action_storage_group_chat_iuvite";
    public static final String ACTION_CHANGE_ALL_MESSAGE_READ_STATE = "action_change_all_message_read_state";
    public static final String ACTION_CHANGE_SPECIFIC_MESSAGE_READ_STATE ="action_change_specific_message_read_state";
    public static final String ACTION_INVITE_TO_GROUP_CHAT = "invite_to_group_chat";
    String chatTableName;
    ContentResolver resolver;
    String arg;
    String chatId;
    int chatType;
    Cursor cursor;
    Gson gson;
    ArrayList<String> messageIdList;
    public void task(Intent intent, Context context ) throws JSONException {
        switch ( intent.getAction() )  {
            case ACTION_STORAGE_GROUP_CHAT_INVITE:
                arg = intent.getStringExtra("info");
                gson = new GsonBuilder()
                        .registerTypeAdapter(HashMap.class, new HashMapDeserializer())
                        .create();
                GroupChatRoom groupChatRoom = null;
                try {
                    JsonObject object = new JsonParser().parse( arg ).getAsJsonObject();
                    JsonElement element = object.get("chatRoom");
                    groupChatRoom = gson.fromJson(element, GroupChatRoom.class);
                } catch ( JsonParseException e) {
                    e.printStackTrace();
                }
                Utils.ChatInitialize(context, groupChatRoom);
                EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, null));
                break;
            case ACTION_STORAGE_GROUP_CHAT_DATA:
                arg = intent.getStringExtra("info");
                storeChatData(arg, context);
                break;
            case ACTION_STORAGE_PERSONAL_CHAT_DATA:
                arg = intent.getStringExtra("info");
                handleStorePersonalChatData(arg, context);
                break;
            case ACTION_HANDLE_INVITE_RESULT :
//                ContentValues contentValues = new ContentValues();
//                chatTableName = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
//                boolean result = intent.getBooleanExtra("result", true);
//                String messageId = intent.getStringExtra(TalkContract.Message.MESSAGE_ID);
//                resolver = context.getContentResolver();
//                contentValues.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, result);
//                int updateResult1 = resolver.update(
//                        TalkContract.ChatRooms.CONTENT_URI,
//                        contentValues,
//                        TalkContract.ChatRooms.CHAT_ID + " = ? ",
//                        new String[] { chatTableName }
//                );
//                contentValues.clear();
//                contentValues.put(TalkContract.Message.IS_SEND, result);
//                int updateResult2 = resolver.update(
//                        TalkContract.Message.CONTENT_URI,
//                        contentValues,
//                        TalkContract.Message.MESSAGE_ID + " = ?",
//                        new String[] { messageId } );
//
//                Log.d(TAG, "task: " + updateResult1 + " : " + updateResult2);
//                EventBus.getDefault().post(new Events.MessageEvent( PersonalChatActivity.EVENT_BUS_ACTION_INVITE_RESULT, chatTableName ));
                break;
            case  ACTION_HANDLE_READ_CHAT:
                messageIdList = new ArrayList<>();
                String info = intent.getStringExtra("info");
                JsonObject obj = new JsonParser().parse(info).getAsJsonObject();
                chatId = obj.get(TalkContract.ChatRooms.CHAT_ID).getAsString();
                JsonArray array = obj.get("messages").getAsJsonArray();
                for ( JsonElement element : array ) {
                    String item = element.getAsString();
                    messageIdList.add(item);
                }

                bulkUpdateMessage(context, messageIdList);
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
                break;

            case ACTION_CHANGE_ALL_MESSAGE_READ_STATE :
                chatId = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                chatType  = intent.getIntExtra("chatType", 1);
                ArrayList<String> notReadMessageList = getNotReadMessages(context, chatId);
                if ( notReadMessageList.size() > 0 ) {
                    if ( chatType == 1 ) {
                        User receiver = intent.getParcelableExtra("receiver");
                        readAllMessage(context, chatType, chatId, receiver, notReadMessageList);
                    } else if ( chatType == 2 ) {
                        readAllMessage(context, chatType, chatId, notReadMessageList);
                    }
                    EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
                }
                break;
            case ACTION_CHANGE_SPECIFIC_MESSAGE_READ_STATE:
                chatId = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                chatType = intent.getIntExtra("chatType", 1);
                String messageId = intent.getStringExtra("messageId");
                ArrayList list = new ArrayList<String>(Arrays.asList(new String[] { messageId }));
                if ( chatType == 1 ) {
                    User receiver = intent.getParcelableExtra("receiver");
                    readAllMessage( context, chatType, chatId, receiver, list );
                } else if ( chatType == 2 ) {
                    readAllMessage( context, chatType, chatId, list );
                }
                break;
            case ACTION_INVITE_TO_GROUP_CHAT:
                arg = intent.getStringExtra("info");
                storeInvitedUserInfo(arg, context);
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_INVITED_USER, null));
                break;
        }
    }
    void bulkUpdateMessage (Context context, ArrayList<String> messageIdList ) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for( String messageId : messageIdList ) {

            Cursor cursor = db.query(TalkContract.Message.TABLE_NAME,
                    new String[] {TalkContract.Message.READING_COUNT},
                    TalkContract.Message.MESSAGE_ID + " = ? ",
                    new String[] { messageId},
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            int count = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.READING_COUNT));
            if ( count > 0 ) {
                ContentValues values = new ContentValues();
                --count;
                values.put(TalkContract.Message.READING_COUNT, count);
                int result = db.update(TalkContract.Message.TABLE_NAME, values, TalkContract.Message.MESSAGE_ID + " = ? ", new String[] { messageId });
                Log.d(TAG, "bulkUpdateMessage: " + result);
            }
        }
        db.close();
    }
    void storeChatData ( String stringify, Context context ) {
        JsonObject object = new JsonParser().parse(stringify).getAsJsonObject();
        JsonElement element = object.get("message");
        Message message = new Gson().fromJson(element, Message.class);
        Utils.insertMessage(context, message, message.getChatId());
        EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, message));
    }
    JsonArray ArrayListToJsonArray ( ArrayList<String> list ) {
        JsonArray array = new JsonArray();
        for ( String item : list ) {
            array.add(item);
        }
        return array;
    }
    void handleStorePersonalChatData ( String arg, Context context ) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .create();

        JsonObject obj = new JsonParser().parse(arg).getAsJsonObject();
        JsonElement messageElement = obj.get("message");
        JsonObject chatRoomElement = obj.get("chatRoom").getAsJsonObject();
        JsonObject senderJsonObj = chatRoomElement.get("talkTo").getAsJsonObject();


        User sender = null;
        PersonalChatRoom chatRoom = null;
        Message message = null;
        try {
            sender = gson.fromJson(senderJsonObj, User.class);
            chatRoom = gson.fromJson(chatRoomElement, PersonalChatRoom.class);
            message = gson.fromJson(messageElement, Message.class);
        } catch ( JsonSyntaxException e ) {
            e.printStackTrace();
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor chatRoomQueryCursor = resolver.query(
                TalkContract.ChatRooms.CONTENT_URI,
                null,
                TalkContract.ChatRooms.CHAT_ID + "= ?",
                new String[] { chatRoom.getChatId() } ,
                null
        );
        Cursor userQueryCursor = resolver.query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ? ",
                new String [] { sender.getId() },
                null
        );


        if( !(chatRoomQueryCursor.getCount() > 0) ) {
            Log.d(TAG, "task: ChatInitialize ");
            Utils.ChatInitialize( context, chatRoom);
            if ( !(userQueryCursor.getCount() > 0) ) {
                Utils.insertUser(context, sender);
            }
        }
        Utils.insertMessage(context, message, chatRoom.getChatId());
        EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, message));
    }
    private void readAllMessage(Context context, int chatType, String chatId, ArrayList<String> messages ) {
        Socket socket = ((MyApp)context.getApplicationContext()).getSocket();
        String emitParam = Utils.groupChatReadObjCreator(chatType, Utils.getMyInfo(context), chatId, messages );

        if ( socket != null && socket.connected() ) {
            socket.emit("chat_read", emitParam, new Ack() {
                @Override
                public void call(Object... args) {

                }
            });
        }
        if( messages.size() > 0 ) {
            bulkUpdateMessage(context, messages);
        }
    }
    private void readAllMessage(Context context, int chatType, String chatId, User receiver, ArrayList<String> messages ) {
        Socket socket = ((MyApp)context.getApplicationContext()).getSocket();
        String emitParam = Utils.personalChatReadObjCreator(chatType, Utils.getMyInfo(context), receiver, chatId, messages);
        if ( socket != null && socket.connected() ) {
            socket.emit("chat_read", emitParam, new Ack() {
                @Override
                public void call(Object... args) {

                }
            });
        }
        if( messages.size() > 0 ) {
            bulkUpdateMessage(context, messages);
        }
    }

    private ArrayList<String> getNotReadMessages(Context context, String chatId ) {
        User myInfo = Utils.getMyInfo(context);
        ArrayList<String> result = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                TalkContract.Message.CONTENT_URI,
                new String[] { TalkContract.Message.MESSAGE_ID },
                "NOT " + TalkContract.Message.CREATOR_ID + " = ? and " + TalkContract.ChatRooms.CHAT_ID + " = ? and " + TalkContract.Message.READING_COUNT + " > 0 ",
                new String[] { myInfo.getId(), chatId },
                null);

        while( cursor.moveToNext() ) {
            result.add(cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_ID)));
        }
        return result;
    }
    private void storeInvitedUserInfo ( String arg, Context context ) {
        gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .create();
        JsonObject object = new JsonParser().parse(arg).getAsJsonObject();
        String chatId = object.get(TalkContract.ChatRooms.CHAT_ID).getAsString();
        ArrayList<User> users = gson.fromJson(object.get("users"), new TypeToken<ArrayList<User>>(){}.getType());
        for ( User user : users ) {
            if ( !Utils.checkUserInDb( context, user.getId()) ) {
                Utils.insertUser(context, user);
            }
        }
        Utils.insertChatMembers(context.getContentResolver(), chatId, users);

    }
}
