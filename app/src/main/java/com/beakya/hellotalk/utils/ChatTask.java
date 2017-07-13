package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.activity.GroupChatActivity;
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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String ACTION_READ_INITIAL_STATE = "action_read_all_chat";
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
                Log.d(TAG, "task: ACTION_STORAGE_GROUP_CHAT_INVITE ");
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
                Log.d(TAG, "task: ACTION_STORAGE_GROUP_CHAT_DATA");
                arg = intent.getStringExtra("info");
                storeChatData(arg, context);
                break;
            case ACTION_STORAGE_PERSONAL_CHAT_DATA:
                Log.d(TAG, "task: ACTION_STORAGE_PERSONAL_CHAT_DATA");
                arg = intent.getStringExtra("info");
                handleStorePersonalChatData(arg, context);
                break;
            case  ACTION_HANDLE_READ_CHAT:
                Log.d(TAG, "task: ACTION_HANDLE_READ_CHAT");
                messageIdList = new ArrayList<>();
                String info = intent.getStringExtra("info");
                JsonObject obj = new JsonParser().parse(info).getAsJsonObject();
                chatId = obj.get(TalkContract.ChatRooms.CHAT_ID).getAsString();
                JsonArray array = obj.get("messages").getAsJsonArray();
                for ( JsonElement element : array ) {
                    String item = element.getAsString();
                    messageIdList.add(item);
                }

                MsgUtils.bulkUpdateCountOfMessage(context, messageIdList);
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
                break;

            case ACTION_CHANGE_ALL_MESSAGE_READ_STATE :
                Log.d(TAG, "task: ACTION_CHANGE_ALL_MESSAGE_READ_STATE");
                chatId = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                chatType  = intent.getIntExtra("chatType", 1);
                ArrayList<String> notReadMessageList = MsgUtils.getNotReadMessages(context, chatId);
                if ( notReadMessageList.size() > 0 ) {
                    if ( chatType == 1 ) {
                        User receiver = intent.getParcelableExtra("receiver");
                        MsgUtils.readAllMessage(context, chatType, chatId, receiver, notReadMessageList);
                    } else if ( chatType == 2 ) {
                        MsgUtils.readAllMessage(context, chatType, chatId, notReadMessageList);
                    }
                    EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
                }
                break;
            case ACTION_CHANGE_SPECIFIC_MESSAGE_READ_STATE:
                Log.d(TAG, "task: ACTION_CHANGE_ALL_MESSAGE_READ_STATE");
                chatId = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                chatType = intent.getIntExtra("chatType", 1);
                String messageId = intent.getStringExtra("messageId");
                ArrayList list = new ArrayList<String>(Arrays.asList(new String[] { messageId }));
                if ( chatType == 1 ) {
                    User receiver = intent.getParcelableExtra("receiver");
                    MsgUtils.readAllMessage( context, chatType, chatId, receiver, list );
                } else if ( chatType == 2 ) {
                    MsgUtils.readAllMessage( context, chatType, chatId, list );
                }
                break;
            case ACTION_INVITE_TO_GROUP_CHAT:
                Log.d(TAG, "task: ACTION_CHANGE_ALL_MESSAGE_READ_STATE");
                arg = intent.getStringExtra("info");
                storeInvitedUserInfo(arg, context);
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_INVITED_USER, null));
                break;
            case ACTION_READ_INITIAL_STATE:
                Log.d(TAG, "task: ACTION_READ_INITIAL_STATE");
                arg = intent.getStringExtra("info");
                storeAllEvents(context, arg);
                break;
        }
    }

    public void storeChatData(String stringify, Context context) {
        JsonObject object = new JsonParser().parse(stringify).getAsJsonObject();
        JsonElement element = object.get("message");
        Message message = new Gson().fromJson(element, Message.class);
        Utils.insertMessage(context, message, message.getChatId(), false);
        EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, message));
    }
    JsonArray ArrayListToJsonArray ( ArrayList<String> list ) {
        JsonArray array = new JsonArray();
        for ( String item : list ) {
            array.add(item);
        }
        return array;
    }
    public void handleStorePersonalChatData ( String arg, Context context ) {
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
        Utils.insertMessage(context, message, chatRoom.getChatId(), false);
        EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, message));
    }




    private void storeInvitedUserInfo ( String arg, Context context ) {
        User myInfo = Utils.getMyInfo(context);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .create();
        JsonObject object = new JsonParser().parse(arg).getAsJsonObject();
        String chatId = object.get(TalkContract.ChatRooms.CHAT_ID).getAsString();
        ArrayList<User> users = gson.fromJson(object.get("users"), new TypeToken<ArrayList<User>>(){}.getType());
        String sender = object.get("sender").getAsString();
        User senderUserObj = Utils.findUser(context, sender);
        for ( User user : users ) {
            if ( !Utils.checkUserInDb( context, user.getId()) ) {
                Utils.insertUser(context, user);
            }
            String messageId = Utils.hashFunction(myInfo.getId() + chatId + System.currentTimeMillis(), "SHA-1");
            Message message = new Message(messageId, "system", senderUserObj.getName() + "님이 " + user.getName() +"님을 초대했습니다.", chatId, TalkContract.Message.TYPE_TEXT, Utils.getCurrentTime(), false,0);
            Utils.insertMessage(context, message, chatId, true);
        }
        Utils.insertChatMembers(context.getContentResolver(), chatId, users);
        EventBus.getDefault().post(new Events.UserInviteEvent(GroupChatActivity.EVENT_INVITED_USER, users));
    }
    private void storeAllEvents ( Context context, String arg ) {
        ContentResolver resolver = context.getContentResolver();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .registerTypeAdapter(Message.class, new MessageDeserializer())
                .serializeNulls()
                .create();
        JsonParser jsonParser = new JsonParser();
        JsonObject rootObject = jsonParser.parse(arg).getAsJsonObject();
        String event = rootObject.get("event").getAsString();

        if ( event.equals("user") ) {
            JsonObject payload = rootObject.get("payload").getAsJsonObject();
            User user = gson.fromJson(payload, User.class);
            Utils.insertUser(context, user);
        } else if ( event.equals("chatRoom") ) {
            JsonObject payload = rootObject.get("payload").getAsJsonObject();
            String chatId = payload.get("chat_id").getAsString();
            int chatType = payload.get("chat_type").getAsInt();
            String chatName = null;
            if (payload.has("chat_name")) {
                JsonElement element = payload.get("chat_name");
                if ( element instanceof JsonNull ) {
                    chatName = "";
                } else {
                    chatName = element.getAsString();
                }
            }
            if ( chatType == 1 ) {
                Utils.insertChatRoom(resolver, new PersonalChatRoom(chatId, chatType, true, null));
            } else if ( chatType == 2 ) {
                Utils.insertChatRoom(resolver, new GroupChatRoom(chatName, null, chatId, chatType, true));
            }
        } else if ( event.equals("message") ) {
            JsonObject payload = rootObject.get("payload").getAsJsonObject();
            Message message = gson.fromJson(payload, Message.class);
            JsonElement element = payload.get("read_time");
            boolean isRead = element instanceof JsonNull ? false : true;
            Utils.insertMessage(context, message, message.getChatId(), isRead);
        } else if ( event.equals("chat_members") ) {
            JsonObject payload = rootObject.get("payload").getAsJsonObject();
            String chatId = payload.get("chat_id").getAsString();
            String userId = payload.get("user_id").getAsString();
            Utils.insertChatMembers(resolver, chatId, Arrays.asList(new User[] { new User(userId, null, null)}));
        } else if ( event.equals("end") ) {
            EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, null));
        }
    }
}
