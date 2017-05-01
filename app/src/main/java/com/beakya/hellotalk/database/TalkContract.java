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
        public static final String USER_PROFILE_IMAGE_PATH ="user_image";
        public static final String USER_TABLE_CREATE_STATEMENT =
                " CREATE TABLE " + TABLE_NAME         + " ( " +
                        USER_ID            + " TEXT PRIMARY KEY, " +
                        USER_NAME          + " TEXT, " +
                        USER_ADDED_TIME    + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        USER_PROFILE_IMAGE_PATH + " TEXT " +
                        " ); ";

    }
    public static final class Chat implements BaseColumns{

        public static Uri generateCreateTableUri ( String tableName ) {
            return BASE_URI.buildUpon().appendPath(PATH).appendPath(tableName).build();
        }

        public static final String TYPE_TEXT = "text";
        public static final String TYPE_IMAGE = "image";

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(Chat.PATH).build();
        public static final String TABLE_NAME = "chat";
        public static final String PATH = TABLE_NAME;
        public static final String SPEAKER = "speaker";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String MESSAGE_CONTENT = "message_content";
        public static final String SPEAKING_TIME = "speaking_time";
        public static final String CHAT_TABLE_CREATE_STATEMENT =
                " CREATE TABLE [" + TABLE_NAME + "] ( " +
                            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            ChatList.CHAT_LIST_ID + " TEXT , " +
                            SPEAKER + " TEXT , " +
                            MESSAGE_CONTENT + " TEXT , " +
                            MESSAGE_TYPE + " TEXT , " +
                            SPEAKING_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                            " ); ";
    }
    public static final class ChatList {
        public static final String TABLE_NAME = "chat_list";
        public static final String PATH = TABLE_NAME;
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(ChatList.PATH).build();
        public static final String CHAT_LIST_ID = "chat_list_id";
        public static final String CREATED_TIME = "created_time";
        public static final String CHAT_LIST_TABLE_CREATE_STATEMENT =
                " CREATE TABLE [" + TABLE_NAME + "] ( " +
                        CHAT_LIST_ID + " TEXT PRIMARY KEY , " +
                        CREATED_TIME + " TIMESTAMP DEFAULT CURRENT_TIME " +
                        " ); ";
    }
    public static final class ChatRoomMembers implements BaseColumns {
        public static final String TABLE_NAME = "chat_members";
        public static final String PATH = TABLE_NAME;
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(ChatRoomMembers.PATH).build();
        public static final String CHAT_ROOM_MEMBERS_TABLE_CREATE_STATEMENT =
                " CREATE TABLE " + TABLE_NAME + " ( " +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ChatList.CHAT_LIST_ID + " TEXT, " +
                        User.USER_ID + " TEXT " +
                        " ); ";

    }
}
