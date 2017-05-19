package com.beakya.hellotalk.asynctaskloader;


import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatListAsyncTaskLoader extends AsyncTaskLoader<ArrayList<ChatRoom>> {
    private static final String TAG = ChatListAsyncTaskLoader.class.getSimpleName();
    final PackageManager packageManager;
    private ArrayList<ChatRoom> chatRoomList = null;
    public ChatListAsyncTaskLoader(Context context) {
        super(context);
        this.packageManager  = getContext().getPackageManager();
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading: ");
        super.onStartLoading();
        if( chatRoomList != null ) {
            deliverResult(chatRoomList);
        } else {
            forceLoad();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public ArrayList<ChatRoom> loadInBackground() {
        Context context = getContext();
        DbHelper dbHelper = new DbHelper(getContext());
        chatRoomList = new ArrayList<>();
        String roomId = TalkContract.ChatRooms.CHAT_LIST_ID;

        String myId = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE).getString(context.getString(R.string.user_id), null);
        String query = " SELECT " + " a.*, " + " ch." + TalkContract.ChatRooms.CHAT_ROOM_TYPE + " from " + TalkContract.ChatRooms.TABLE_NAME + " as ch " +
                " join " + TalkContract.Chat.TABLE_NAME + " as a ON " + "a."+ TalkContract.Chat._ID + " = " +
                " ( " + "SELECT " + " b." + TalkContract.Chat._ID + " from " + TalkContract.Chat.TABLE_NAME  + " as b WHERE " + " b."+roomId + " = " + "ch." +roomId +
                " ORDER BY " + "b."+ TalkContract.Chat._ID + " DESC limit 1 " + " ) ";
        Log.d(TAG, "loadInBackground query :" + query);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null, null);
        while( cursor.moveToNext()) {
            db.beginTransaction();
            //채팅 id
            String chatRoomId = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_LIST_ID));
            // 채팅 유저 쿼리
            String roomUsersQuery = " SELECT " + " b."+TalkContract.User.USER_NAME +" , " + " a."+TalkContract.User.USER_ID + " FROM " + TalkContract.ChatUserRooms.TABLE_NAME + " as a "+
                    " INNER JOIN " + TalkContract.User.TABLE_NAME + " as b ON " + " a."+ TalkContract.User.USER_ID + " = " + " b." + TalkContract.User.USER_ID + " WHERE " +
                    "a."+ TalkContract.ChatRooms.CHAT_LIST_ID + " = " +"'"+ chatRoomId+"'";

            Log.d(TAG, "loadInBackground roomUsersQuery :" + roomUsersQuery);
            Cursor roomUsers = db.rawQuery(roomUsersQuery, null, null);
            Cursor notReadChatCountQuery = db.query(
                    TalkContract.Chat.TABLE_NAME,
                    new String[] {"count(*) as count"},
                    " NOT "+TalkContract.Chat.CREATOR_ID +" = ? and " + TalkContract.ChatRooms.CHAT_LIST_ID +"=? and "+ TalkContract.Chat.IS_READ + "=?",
                    new String[] {myId, chatRoomId, "0"},
                    null,
                    null,
                    null);
            Log.d(TAG, "loadInBackground: " + chatRoomId);
            Log.d(TAG, "loadInBackground: " + roomUsersQuery);
            String date = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CREATED_TIME));


            int notReadChatCount = 0;
            if( notReadChatCountQuery.moveToNext()) {
                notReadChatCount = notReadChatCountQuery.getInt(notReadChatCountQuery.getColumnIndex("count"));
            }
            Log.d(TAG, "loadInBackground: notReadChatCount " + notReadChatCount);
            ArrayList<User> userList = new ArrayList<>();
            String lastContent = cursor.getString(cursor.getColumnIndex(TalkContract.Chat.MESSAGE_CONTENT));
            int chatType = cursor.getInt(cursor.getColumnIndex(TalkContract.Chat.MESSAGE_TYPE));
            int chatRoomType = cursor.getInt(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_ROOM_TYPE));
            String lastChatTimeMessage = Utils.timeToString(date);
            while( roomUsers.moveToNext() ) {
                userList.add(new User(roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID)),
                        roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_NAME))));
            }
            chatRoomList.add(new ChatRoom(userList, chatRoomId, lastContent, chatType, chatRoomType, notReadChatCount, lastChatTimeMessage));
            db.endTransaction();
        }

        return chatRoomList;
    }

    @Override
    public void deliverResult(ArrayList<ChatRoom> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset: ");
        super.onReset();
        onStartLoading();

    }

    @Override
    public void onCanceled(ArrayList<ChatRoom> data) {
        super.onCanceled(data);
    }

    private void releaseResources( HashMap<String, ChatRoom> data ) {

    }
}
