package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;

import java.util.ArrayList;

import io.socket.client.Ack;
import io.socket.client.Socket;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class MsgUtils {
    private static final String TAG = MsgUtils.class.getSimpleName();
    public static ArrayList<String> getNotReadMessages(Context context, String chatId ) {
        User myInfo = Utils.getMyInfo(context);
        ArrayList<String> result = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                TalkContract.Message.CONTENT_URI,
                new String[] { TalkContract.Message.MESSAGE_ID },
                "NOT " + TalkContract.Message.CREATOR_ID + " = ? " +" and NOT " + TalkContract.Message.CREATOR_ID + " = ? and " + TalkContract.ChatRooms.CHAT_ID + " = ? and " + TalkContract.Message.IS_READ + " = 0 ",
                new String[] {"system", myInfo.getId(), chatId },
                null);

        while( cursor.moveToNext() ) {
            result.add(cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_ID)));
        }
        return result;
    }
    public static void readAllMessage(Context context, int chatType, String chatId, User receiver, ArrayList<String> messages ) {
        Socket socket = ((MyApp)context.getApplicationContext()).getSocket();
        String emitParam = Utils.personalChatReadObjCreator(chatType, Utils.getMyInfo(context), receiver, chatId, messages);
        if ( socket != null && socket.connected() && messages.size() > 0 ) {
            Log.d(TAG, "readAllMessage: " + messages.size());
            socket.emit("chat_read", emitParam, new Ack() {
                @Override
                public void call(Object... args) {

                }
            });
        }
        if( messages.size() > 0 ) {
            bulkUpdateCountOfMessage(context, messages);
            bulkUpdateReadStateOfMessage(context, messages);
        }
    }
    public static void readAllMessage(Context context, int chatType, String chatId, ArrayList<String> messages ) {
        Socket socket = ((MyApp)context.getApplicationContext()).getSocket();
        String emitParam = Utils.groupChatReadObjCreator(chatType, Utils.getMyInfo(context), chatId, messages );

        if ( socket != null && socket.connected() && messages.size() > 0 ) {
            Log.d(TAG, "readAllMessage: " + messages.size());
            socket.emit("chat_read", emitParam, new Ack() {
                @Override
                public void call(Object... args) {

                }
            });
        } else {
            Log.d(TAG, "readAllMessage: socket not connected");
        }
        if( messages.size() > 0 ) {
            bulkUpdateCountOfMessage(context, messages);
            bulkUpdateReadStateOfMessage(context, messages);
        }
    }
    public static void bulkUpdateReadStateOfMessage (Context context, ArrayList<String> messageIdList) {
        ContentResolver resolver = context.getContentResolver();
        for ( String messageId : messageIdList ) {
            ContentValues value = new ContentValues();
            value.put(TalkContract.Message.IS_READ, 1);
            resolver.update(
                    TalkContract.Message.CONTENT_URI,
                    value,
                    TalkContract.Message.MESSAGE_ID + " = ? ",
                    new String[] { messageId }
            );
        }
    }
    public static void bulkUpdateCountOfMessage(Context context, ArrayList<String> messageIdList ) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for( String messageId : messageIdList ) {

            Cursor cursor = db.query(TalkContract.Message.TABLE_NAME,
                    new String[] {TalkContract.Message.READING_COUNT},
                    TalkContract.Message.MESSAGE_ID + " = ? ",
                    new String[] { messageId },
                    null,
                    null,
                    null);
            while ( cursor.moveToNext() ) {
                int count = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.READING_COUNT));
                if ( count > 0 ) {
                    ContentValues values = new ContentValues();
                    --count;
                    values.put(TalkContract.Message.READING_COUNT, count);
                    int result = db.update(TalkContract.Message.TABLE_NAME, values, TalkContract.Message.MESSAGE_ID + " = ? ", new String[] { messageId });
                    Log.d(TAG, "bulkUpdateCountOfMessage: " + result);
                }
            }
        }
        db.close();
    }
}
