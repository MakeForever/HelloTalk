package com.beakya.hellotalk.asynctaskloader;


import android.content.Context;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatListAsyncTaskLoader extends AsyncTaskLoader<ArrayList<ChatListItem>> {
    private static final String TAG = ChatListAsyncTaskLoader.class.getSimpleName();


    public ChatListAsyncTaskLoader(Context context) {
        super(context);
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
        ArrayList<ChatListItem> chatRoomList = new ArrayList<>();
        String chatId = TalkContract.ChatRooms.CHAT_ID;
//        String chatType = TalkContract.ChatRooms.CHAT_ROOM_TYPE;
//        String messageId = TalkContract.Message.MESSAGE_ID;
//        String messageContent = TalkContract.Message.MESSAGE_CONTENT;
//        String MessageTable = TalkContract.Message.TABLE_NAME;
//        String chatListTable = TalkContract.ChatRooms.TABLE_NAME;
//        String createdTime = TalkContract.Message.CREATED_TIME;
        String myId = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE).getString(context.getString(R.string.user_id), null);
//        String lastChatQuery = "SELECT "+"ch."+ chatType + ", m.*" +" FROM " + chatListTable + " as ch " + " JOIN " + MessageTable + " as m " + " ON "+"m."+ messageId + " = " +
//                "( SELECT " + mesageId + " FROM " + MessageTable + " AS m2 " +" WHERE " + "m2."+ chatId + " = " + "ch."+chatId + " order by " + "m2."+ createdTime + " DESC LIMIT 1 )";
//        Log.d(TAG, "loadInBackground query :" + lastChatQuery);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TalkContract.ChatRooms.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                TalkContract.ChatRooms.LAST_MESSAGE_RECEIVE_TIME + " desc",
                null
                );
        while( cursor.moveToNext()) {
            //채팅 id
            String mChatId = cursor.getString(cursor.getColumnIndex(chatId));
            // 채팅 유저 쿼리
            String roomUsersQuery = " SELECT " + " b.*, a."+ TalkContract.ChatRoomUsers.IS_MEMBER+" FROM " + TalkContract.ChatRoomUsers.TABLE_NAME + " as a "+
                    " INNER JOIN " + TalkContract.User.TABLE_NAME + " as b ON " + " a."+ TalkContract.User.USER_ID + " = " + " b." + TalkContract.User.USER_ID + " WHERE " +
                    "a."+ TalkContract.ChatRooms.CHAT_ID + " = " +"'"+ mChatId+"'";

            Cursor roomUsers = db.rawQuery(roomUsersQuery, null);
            Cursor notReadChatCountQuery = db.query(
                    TalkContract.Message.TABLE_NAME,
                    new String[] {"count(*) as count"},
                    " NOT "+ TalkContract.Message.CREATOR_ID +" = ? and " + TalkContract.ChatRooms.CHAT_ID +" = ? and "+ TalkContract.Message.IS_READ + " = ?",
                    new String[] { myId, mChatId, "0" },
                    null,
                    null,
                    null
            );
            Cursor lastMessage = db.query(
                    TalkContract.Message.TABLE_NAME,
                    null,
                    TalkContract.ChatRooms.CHAT_ID + " = ?",
                    new String[] { mChatId },
                    null,
                    null,
                    TalkContract.Message.CREATED_TIME + " desc",
                    "1"
            );

            Log.d(TAG, "loadInBackground: " + mChatId);
            Log.d(TAG, "loadInBackground: " + roomUsersQuery);



//            String lastMessageId = lastMessage.getString(lastMessage.getColumnIndex(TalkContract.Message.CREATOR_ID)););
            int notReadChatCount = 0;
            if( notReadChatCountQuery.moveToNext()) {
                notReadChatCount = notReadChatCountQuery.getInt(notReadChatCountQuery.getColumnIndex("count"));
            }
            Log.d(TAG, "loadInBackground: notReadChatCount "+ mChatId+ " : " + notReadChatCount);

            String mLastMessage = null;
            int mMessageType = 0;
            String date;
            if ( lastMessage.getCount() > 0 ) {
                lastMessage.moveToFirst();
                mLastMessage = lastMessage.getString(lastMessage.getColumnIndex(TalkContract.Message.MESSAGE_CONTENT));
                String[] mLastMessageSplitByEnter = mLastMessage.split("\n");
                if ( mLastMessageSplitByEnter.length > 2 ) {
                    mLastMessage = mLastMessageSplitByEnter[0] + "\n" + mLastMessageSplitByEnter[1] + "\n" + "...";
                }
                mMessageType = lastMessage.getInt(lastMessage.getColumnIndex(TalkContract.Message.MESSAGE_TYPE));
                date = lastMessage.getString(lastMessage.getColumnIndex(TalkContract.Message.CREATED_TIME));
            } else {
                date = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CREATED_TIME));
            }


            int mChatRoomType = cursor.getInt(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_ROOM_TYPE));


            ChatRoom chatRoom;
            if ( mChatRoomType == 1) {
                ArrayList<User> userList = new ArrayList<>();
                while( roomUsers.moveToNext() ) {
                    userList.add(
                            new User(
                                    roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID)),
                                    roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_NAME)),
                                    roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0,
                                    roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.ChatRoomUsers.IS_MEMBER)) > 0
                            )
                    );

                }
                chatRoom = new PersonalChatRoom(mChatId, mChatRoomType, true, userList.get(0));
            } else {
                String chatName = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_NAME));
                HashMap<String, User> userList = new HashMap<>();
                while( roomUsers.moveToNext() ) {
                    userList.put(
                            roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID))
                            ,new User(
                                    roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID)),
                                    roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_NAME)),
                                    roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0,
                                    roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.ChatRoomUsers.IS_MEMBER)) > 0
                            )
                    );
                }
                chatRoom = new GroupChatRoom(chatName, userList, mChatId, mChatRoomType, true);
            }
            chatRoomList.add(new ChatListItem(chatRoom, mLastMessage, mMessageType, date, notReadChatCount));
            roomUsers.close();
            notReadChatCountQuery.close();
            lastMessage.close();
        }

        cursor.close();
        db.close();

        return chatRoomList;
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
