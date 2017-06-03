package com.beakya.hellotalk.asynctaskloader;


import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatListItem;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatListAsyncTaskLoader extends AsyncTaskLoader<ArrayList<ChatListItem>> {
    private static final String TAG = ChatListAsyncTaskLoader.class.getSimpleName();
    final PackageManager packageManager;
    private ArrayList<ChatListItem> groupChatRoomList = null;
    public ChatListAsyncTaskLoader(Context context) {
        super(context);
        this.packageManager  = getContext().getPackageManager();
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading: ");
        super.onStartLoading();
        forceLoad();
    }


    @Override
    public ArrayList<ChatListItem> loadInBackground() {
        Context context = getContext();
        DbHelper dbHelper = new DbHelper(getContext());
        groupChatRoomList = new ArrayList<>();
        String chatId = TalkContract.ChatRooms.CHAT_ID;
        String chatType = TalkContract.ChatRooms.CHAT_ROOM_TYPE;
        String messageId = TalkContract.Message.MESSAGE_ID;
        String messageContent = TalkContract.Message.MESSAGE_CONTENT;
        String MessageTable = TalkContract.Message.TABLE_NAME;
        String chatListTable = TalkContract.ChatRooms.TABLE_NAME;
        String createdTime = TalkContract.Message.CREATED_TIME;
        String myId = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE).getString(context.getString(R.string.user_id), null);
        String query = "SELECT "+"ch."+ chatType + ", m.*" +" FROM " + chatListTable + " as ch " + " JOIN " + MessageTable + " as m " + " ON "+"m."+ messageId + " = " +
                "( SELECT " + messageId + " FROM " + MessageTable + " AS m2 " +" WHERE " + "m2."+ chatId + " = " + "ch."+chatId + " order by " + "m2."+ createdTime + " DESC LIMIT 1 )";
        Log.d(TAG, "loadInBackground query :" + query);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while( cursor.moveToNext()) {

            //채팅 id
            String mChatId = cursor.getString(cursor.getColumnIndex(chatId));
            // 채팅 유저 쿼리
            String roomUsersQuery = " SELECT " + " b.* "+" FROM " + TalkContract.ChatUserRooms.TABLE_NAME + " as a "+
                    " INNER JOIN " + TalkContract.User.TABLE_NAME + " as b ON " + " a."+ TalkContract.User.USER_ID + " = " + " b." + TalkContract.User.USER_ID + " WHERE " +
                    "a."+ TalkContract.ChatRooms.CHAT_ID + " = " +"'"+ mChatId+"'";

            Log.d(TAG, "loadInBackground roomUsersQuery :" + roomUsersQuery);
            Cursor roomUsers = db.rawQuery(roomUsersQuery, null);
            Cursor notReadChatCountQuery = db.query(
                    TalkContract.Message.TABLE_NAME,
                    new String[] {"count(*) as count"},
                    " NOT "+ TalkContract.Message.CREATOR_ID +" = ? and " + TalkContract.ChatRooms.CHAT_ID +"=? and "+ TalkContract.Message.READING_COUNT + "=?",
                    new String[] {myId, mChatId, "0"},
                    null,
                    null,
                    null);
            Log.d(TAG, "loadInBackground: " + mChatId);
            Log.d(TAG, "loadInBackground: " + roomUsersQuery);



            int notReadChatCount = 0;
            if( notReadChatCountQuery.moveToNext()) {
                notReadChatCount = notReadChatCountQuery.getInt(notReadChatCountQuery.getColumnIndex("count"));
            }
            Log.d(TAG, "loadInBackground: notReadChatCount " + notReadChatCount);
            ArrayList<User> userList = new ArrayList<>();
            String mLastMessage = cursor.getString(cursor.getColumnIndex(TalkContract.Message.MESSAGE_CONTENT));
            int mMessageType = cursor.getInt(cursor.getColumnIndex(TalkContract.Message.MESSAGE_TYPE));
            int mChatRoomType = cursor.getInt(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_ROOM_TYPE));
            String date = cursor.getString(cursor.getColumnIndex(TalkContract.Message.CREATED_TIME));
            while( roomUsers.moveToNext() ) {
                userList.add(
                        new User(
                                roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID)),
                                roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_NAME)),
                                roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0
                        )
                );

            }
            ChatRoom chatRoom = null;
            if ( mChatRoomType == 1) {
                chatRoom = new PersonalChatRoom(mChatId, mChatRoomType, true, userList.get(0));
            } else {
                //TODO : 그룹챗 만들것
            }
            groupChatRoomList.add(new ChatListItem(chatRoom, mLastMessage, mMessageType, date, notReadChatCount));
        }

        return groupChatRoomList;
    }

    @Override
    public void deliverResult(ArrayList<ChatListItem> data) {
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
        onStopLoading();

    }

    @Override
    public void onCanceled(ArrayList<ChatListItem> data) {
        super.onCanceled(data);
    }

    private void releaseResources( HashMap<String, GroupChatRoom> data ) {

    }
}
