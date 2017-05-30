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
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.Message;

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

                Message message = intent.getParcelableExtra("message");
                ChatRoom chatRoom = intent.getParcelableExtra("chatRoom");
                resolver = context.getContentResolver();
                Cursor cursor = resolver.query(
                                TalkContract.ChatRooms.CONTENT_URI,
                                null,
                                TalkContract.ChatRooms.CHAT_ID + "= ?",
                                new String[] { chatRoom.getChatId() } ,
                                null
                        );
                if( !(cursor.getCount() > 0) && chatRoom.getMembersCount() > 0 ) {
                    Log.d(TAG, "task: ChatInitialize ");
                    Utils.ChatInitialize( context, chatRoom );
                }
                Utils.insertMessage(context, message, chatRoom.getChatId());
                break;

            case ACTION_CHAT_SEND_RESULT:
                Log.d(TAG, "Message Task // ACTION_CHAT_SEND_RESULT execute ");
                JSONObject responseData = new JSONObject(intent.getStringExtra("data"));
                chatTableName = responseData.getString("chatTableName");
                boolean result = responseData.getBoolean("result");
                int insertedChatRowNumber = responseData.getInt("insertedChatRowNumber");
                resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, result);
                //TODO IS_SYNCHRONIZED 이거 먼저 체크 하고 안되어있으면 바꾸든지 해야 할거같다
                int updatedRow = resolver.update(TalkContract.ChatRooms.CONTENT_URI, values, TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] {chatTableName});
                if( updatedRow < 1 ) {
                    throw new RuntimeException("something wrong");
                }
                values.clear();
                values.put(TalkContract.Message.IS_SEND, true);
                int insertedRowUpdated = resolver.update(TalkContract.Message.CONTENT_URI, values, TalkContract.Message._ID + "= ?", new String[]{String.valueOf(insertedChatRowNumber)});
                Log.d(TAG, "insertedRowUpdated: " + insertedChatRowNumber + " just updated to " + insertedRowUpdated);
                EventBus.getDefault().post(new MessageEvent<ChatResultEvent<Boolean>>("message_send_success", new ChatResultEvent("message_send_success", chatTableName, result)));
                break;
        }
    }
}
