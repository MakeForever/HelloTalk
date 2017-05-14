package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.events.ChatResultEvent;
import com.beakya.hellotalk.events.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatTask {
    public static final String TAG = ChatTask.class.getSimpleName();
    public static final String ACTION_CHAT_SEND_RESULT = "action_invite_chat_success";
    public static final String ACTION_STORAGE_CHAT_DATA = "action_storage_chat_data";
    String chatTableName;
    String messageContent;
    String sender;
    String messageType;
    ContentResolver resolver;
    public void task(Intent intent, Context context ) throws JSONException {
        switch ( intent.getAction() )  {
            case ACTION_STORAGE_CHAT_DATA :

                JSONObject obj = new JSONObject(intent.getStringExtra("data"));
                chatTableName = obj.getString("chatTableName");
                JSONArray members = null;
                if( obj.has("members")) {
                    members = obj.getJSONArray("members");
                }
                messageContent = obj.getString("message_content");
                messageType = obj.getString("message_type");
                sender = obj.getString(TalkContract.Chat.CREATOR_ID);

                int chatType = obj.getInt(TalkContract.ChatRoom.CHAT_TYPE);

                if ( chatTableName == null || messageContent == null || messageType == null || sender == null ) {
                    return;
                }
                resolver = context.getContentResolver();
                Cursor cursor = resolver.query(
                                TalkContract.ChatRoom.CONTENT_URI,
                                null,
                                TalkContract.ChatRoom.CHAT_LIST_ID + "= ?",
                                new String[] { chatTableName } ,
                                null
                        );
                if( !(cursor.getCount() > 0) && members != null ) {
                    Utils.ChatInitialize(context, chatTableName, chatType, Utils.JSONArrayToArrayList(members));
                }

                ContentValues chatParams = new ContentValues();
                chatParams.put(TalkContract.ChatRoom.CHAT_LIST_ID, chatTableName);
                chatParams.put(TalkContract.Chat.CREATOR_ID, sender );
                chatParams.put(TalkContract.Chat.MESSAGE_CONTENT, messageContent);
                chatParams.put(TalkContract.Chat.MESSAGE_TYPE, TalkContract.Chat.TYPE_TEXT);
                resolver.insert(TalkContract.Chat.CONTENT_URI, chatParams);
                EventBus.getDefault().post(new MessageEvent<String>("first_received", chatTableName));
                Log.d(TAG, "task: " + obj.toString());
                break;

            case ACTION_CHAT_SEND_RESULT:
                Log.d(TAG, "Chat Task // ACTION_CHAT_SEND_RESULT execute ");
                JSONObject responseData = new JSONObject(intent.getStringExtra("data"));
                chatTableName = responseData.getString("chatTableName");
                boolean result = responseData.getBoolean("result");
                int insertedChatRowNumber = responseData.getInt("insertedChatRowNumber");
                resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(TalkContract.ChatRoom.IS_SYNCHRONIZED, result);
                int updatedRow = resolver.update(TalkContract.ChatRoom.CONTENT_URI, values, TalkContract.ChatRoom.CHAT_LIST_ID + " = ?", new String[] {chatTableName});
                if( updatedRow < 1 ) {
                    throw new RuntimeException("something wrong");
                }
                values.clear();
                values.put(TalkContract.Chat.IS_SEND, true);
                int insertedRowUpdated = resolver.update(TalkContract.Chat.CONTENT_URI, values, TalkContract.Chat._ID + "= ?", new String[]{String.valueOf(insertedChatRowNumber)});
                Log.d(TAG, "insertedRowUpdated: " + insertedChatRowNumber + " just updated to " + insertedRowUpdated);
                EventBus.getDefault().post(new MessageEvent<ChatResultEvent<Boolean>>("message_send_success", new ChatResultEvent("message_send_success", chatTableName, result)));
                break;
        }
    }
}
