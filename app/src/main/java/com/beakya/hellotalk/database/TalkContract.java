package com.beakya.hellotalk.database;

import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by cheolho on 2017. 3. 23..
 */

public class TalkContract {
    public static final String PROVIDER_SCHEME = "content://";
    public static final String PROVIDER_AUTHORITY = "com.beakya.hellotalk";
    public static final Uri BASE_URI = Uri.parse( PROVIDER_SCHEME + PROVIDER_AUTHORITY );
    public static final class User {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(User.FRIENDS_PATH).build();
        public static final String TABLE_NAME = "users";
        public static final String FRIENDS_PATH = TABLE_NAME;
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_ADDED_TIME = "time";
        public static final String HAVE_PROFILE_IMAGE ="user_image";
        public static final String IS_MY_FRIEND = "is_my_friend";
        public static final String USER_TABLE_CREATE_STATEMENT =
                " CREATE TABLE " + TABLE_NAME         + " ( " +
                        USER_ID            + " TEXT PRIMARY KEY, " +
                        USER_NAME          + " TEXT, " +
                        USER_ADDED_TIME    + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        HAVE_PROFILE_IMAGE + " BOOLEAN DEFAULT 0, " +
                        IS_MY_FRIEND + " BOOLEAN DEFAULT 0 " +
                        " ); ";

    }
    public static final class Message implements BaseColumns{
        public static final String MESSAGE_UPDATE_TRIGGER = "message_trigger";
        public static final int TYPE_TEXT = 1;
        public static final String TYPE_IMAGE = "image";

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(Message.PATH).build();
        public static final String TABLE_NAME = "message";
        public static final String PATH = TABLE_NAME;
        public static final String CREATOR_ID = "creator_id";
        public static final String MESSAGE_ID = "message_id";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String MESSAGE_CONTENT = "message_content";
        public static final String CREATED_TIME = "created_time";
        public static final String IS_SEND = "is_send";
        public static final String READING_COUNT = "reading_count";
        public static final String IS_READ ="is_read";
        public static final String CHAT_TABLE_CREATE_STATEMENT =
                " CREATE TABLE [" + TABLE_NAME + "] ( " +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ChatRooms.CHAT_ID + " TEXT , " +
                        MESSAGE_ID + " TEXT , " +
                        CREATOR_ID + " TEXT , " +
                        MESSAGE_CONTENT + " TEXT , " +
                        MESSAGE_TYPE + " INTEGER , " +
                        CREATED_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        IS_SEND + " BOOLEAN DEFAULT 0, " +
                        READING_COUNT + " INTEGER DEFAULT 0, " +
                        IS_READ + " BOOLEAN DEFAULT 0" +
                            " ); ";
        public static final String MESSAGE_UPDATE_TRIGGER_STATEMENT =
                "CREATE TRIGGER " + MESSAGE_UPDATE_TRIGGER + " AFTER INSERT ON " + TABLE_NAME +
                        " BEGIN " +
                            " UPDATE " + ChatRooms.TABLE_NAME +
                            " SET " + ChatRooms.LAST_MESSAGE_RECEIVE_TIME + " = " + " NEW."+CREATED_TIME +
                            " WHERE " + ChatRooms.CHAT_ID + " = NEW."+ ChatRooms.CHAT_ID + " ; " +
                        " END ";
    }
    public static final class ChatRooms {
        public static final String TABLE_NAME = "chat_list";
        public static final String PATH = TABLE_NAME;
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(ChatRooms.PATH).build();
        public static final String CHAT_ID = "chat_id";
        public static final String CHAT_NAME = "chat_name";
        public static final String CREATED_TIME = "created_time";
        public static final String LAST_MESSAGE_RECEIVE_TIME ="last_message_receive_time";
        public static final String CHAT_ROOM_TYPE = "chat_type";
        public static final String IS_SYNCHRONIZED = "is_synchronized";
        public static final String CHAT_LIST_TABLE_CREATE_STATEMENT =
                " CREATE TABLE [" + TABLE_NAME + "] ( " +
                        CHAT_ID + " TEXT PRIMARY KEY , " +
                        CHAT_NAME + " TEXT, " +
                        CHAT_ROOM_TYPE + " INTEGER DEFAULT 1, " +
                        CREATED_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        LAST_MESSAGE_RECEIVE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        IS_SYNCHRONIZED + " BOOLEAN DEFAULT 0 " +
                        " ); ";
    }
    public static final class ChatRoomUsers implements BaseColumns {
        public static final String TABLE_NAME = "chat_members";
        public static final String PATH = TABLE_NAME;
        public static final String IS_MEMBER = "is_member";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(ChatRoomUsers.PATH).build();
        public static final String CHAT_ROOM_MEMBERS_TABLE_CREATE_STATEMENT =
                " CREATE TABLE " + TABLE_NAME + " ( " +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ChatRooms.CHAT_ID + " TEXT, " +
                        User.USER_ID + " TEXT, " +
                        IS_MEMBER + " BOOLEAN DEFAULT 1 " +
                        " ); ";

    }
}
