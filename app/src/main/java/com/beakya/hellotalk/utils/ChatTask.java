package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.ChatActivity;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static com.beakya.hellotalk.activity.ChatActivity.EVENT_NEW_MESSAGE_ARRIVED;

/**
 * Created by goodlife on 2017. 5. 5..
 */

public class ChatTask {
    public static final String TAG = ChatTask.class.getSimpleName();
    public static final String ACTION_STORAGE_CHAT_DATA = "action_storage_chat_data";
    public static final String ACTION_HANDLE_INVITE_RESULT = "action_handle_invite_result";
    public static final String ACTION_HANDLE_READ_CHAT = "action_handle_read_chat";
    public static final String ACTION_CHANGE_ALL_MESSAGE_READ_STATE = "action_change_all_message_read_state";
    String chatTableName;
    ContentResolver resolver;
    public void task(Intent intent, Context context ) throws JSONException {
        switch ( intent.getAction() )  {
            case ACTION_STORAGE_CHAT_DATA :

                Message message = intent.getParcelableExtra("message");
                PersonalChatRoom chatRoom = intent.getParcelableExtra("chatRoom");
                resolver = context.getContentResolver();
                Cursor cursor = resolver.query(
                                TalkContract.ChatRooms.CONTENT_URI,
                                null,
                                TalkContract.ChatRooms.CHAT_ID + "= ?",
                                new String[] { chatRoom.getChatId() } ,
                                null
                        );
                if( !(cursor.getCount() > 0) ) {
                    Log.d(TAG, "task: ChatInitialize ");
                    Utils.ChatInitialize( context, chatRoom);
                }
                Utils.insertMessage(context, message, chatRoom.getChatId());
                EventBus.getDefault().post(new Events.MessageEvent(EVENT_NEW_MESSAGE_ARRIVED, message));
                break;
            case ACTION_HANDLE_INVITE_RESULT :
                ContentValues contentValues = new ContentValues();
                chatTableName = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                boolean result = intent.getBooleanExtra("result", true);
                String messageId = intent.getStringExtra(TalkContract.Message.MESSAGE_ID);
                resolver = context.getContentResolver();

                contentValues.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, result);

                int updateResult1 = resolver.update(
                        TalkContract.ChatRooms.CONTENT_URI,
                        contentValues,
                        TalkContract.ChatRooms.CHAT_ID + " = ? ",
                        new String[] { chatTableName }
                );

                contentValues.clear();
                contentValues.put(TalkContract.Message.IS_SEND, result);
                int updateResult2 = resolver.update(
                        TalkContract.Message.CONTENT_URI,
                        contentValues,
                        TalkContract.Message.MESSAGE_ID + " = ?",
                        new String[] { messageId } );

                Log.d(TAG, "task: " + updateResult1 + " : " + updateResult2);
//                EventBus.getDefault().post(new Events.MessageEvent( ChatActivity.EVENT_BUS_ACTION_INVITE_RESULT, chatTableName ));
                break;
            case  ACTION_HANDLE_READ_CHAT:
                String param = intent.getStringExtra("object");
                JSONObject obj = new JSONObject(param);
                String chatId = obj.getString(TalkContract.ChatRooms.CHAT_ID);
                ArrayList<String> test = Utils.JSONArrayToArrayList(obj.getJSONArray("message_id_list"), TalkContract.Message.MESSAGE_ID);
                bulkUpdateMessage(context, test);
                EventBus.getDefault().post(new Events.MessageEvent(ChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
                break;

            case ACTION_CHANGE_ALL_MESSAGE_READ_STATE :
                ArrayList<String> messageIdList = new ArrayList<>();
                MyApp myApp = (MyApp) context.getApplicationContext();
                Socket socket = myApp.getSocket();
                chatId = intent.getStringExtra(TalkContract.ChatRooms.CHAT_ID);
                String myId = intent.getStringExtra(TalkContract.User.USER_ID);
                User user = intent.getParcelableExtra("user");
                resolver = context.getContentResolver();
                cursor = resolver.query(
                        TalkContract.Message.CONTENT_URI,
                        new String[] { TalkContract.Message.MESSAGE_ID },
                        "NOT " + TalkContract.Message.CREATOR_ID + " = ? and " + TalkContract.ChatRooms.CHAT_ID + " = ? and " + TalkContract.Message.READING_COUNT + " > 0 ",
                        new String[] { myId, chatId },
                        null
                );
                while( cursor.moveToNext() ) {
                    String t = cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_ID));
                    messageIdList.add(t);
                }
                bulkUpdateMessage(context, messageIdList);
                String messageObj = Utils.createIsReadMessageJsonObj(chatId, messageIdList, user);
                socket.emit("chat_read" , messageObj);
                EventBus.getDefault().post(new Events.MessageEvent(ChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
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
}
