package com.beakya.hellotalk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by cheolho on 2017. 3. 24..
 */

public class DbHelper extends SQLiteOpenHelper {
    private static String dbName = "helloTalk.db";
    private static int version = 1 ;
    public DbHelper(Context context) {
        super(context, dbName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TalkContract.User.USER_TABLE_CREATE_STATEMENT);
        db.execSQL(TalkContract.ChatList.CHAT_LIST_TABLE_CREATE_STATEMENT);
        db.execSQL(TalkContract.ChatRoomMembers.CHAT_ROOM_MEMBERS_TABLE_CREATE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
