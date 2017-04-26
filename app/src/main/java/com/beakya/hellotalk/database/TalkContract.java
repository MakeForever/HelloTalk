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
    public static final class Friend {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(Friend.FRIENDS_PATH).build();
        public static final String TABLE_NAME = "users";
        public static final String FRIENDS_PATH = TABLE_NAME;
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_ADDED_TIME = "time";
        public static final String USER_PROFILE_IMAGE_PATH ="user_image";
        public static final String USER_TABLE_CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME         + " ( " +
                                                                                    USER_ID            + " TEXT PRIMARY KEY , " +
                                                                                    USER_NAME          + " TEXT , " +
                                                                                    USER_ADDED_TIME    + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP ," +
                                                                                    USER_PROFILE_IMAGE_PATH + " TEXT " +
                                                                                                         " ); ";

    }
    public static final class Chat {

    }
    public static final class ConversationTables implements BaseColumns {

    }
    public static final class ChatLists implements BaseColumns {

    }
}
