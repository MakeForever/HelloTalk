package com.beakya.hellotalk.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;

import static com.beakya.hellotalk.database.TalkContract.User.FRIENDS_PATH;

/**
 * Created by cheolho on 2017. 3. 24..
 */

public class UserContentProvider extends ContentProvider {
    public static final String TAG = UserContentProvider.class.getSimpleName();
    public static final int USERS = 100;
    public static final int USER_WITH_ID = 101;
    public static final int CHAT_LIST = 200;
    public static final int CHAT_MEMBERS = 300;
    public static final int CHAT = 400;
    public static final int CHAT_ITEM = 401;
    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        Context mContext = getContext();
        mDbHelper = new DbHelper(mContext);
        return true;
    }

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, FRIENDS_PATH, USERS);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, FRIENDS_PATH + "/*", USER_WITH_ID);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, TalkContract.ChatRooms.PATH, CHAT_LIST);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, TalkContract.ChatUserRooms.PATH, CHAT_MEMBERS);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, TalkContract.Chat.PATH, CHAT);
        uriMatcher.addURI(TalkContract.PROVIDER_AUTHORITY, TalkContract.Chat.PATH + "/#", CHAT_ITEM);
        return uriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case USERS :
                cursor = db.query(TalkContract.User.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CHAT :
                cursor = db.query(TalkContract.Chat.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CHAT_LIST :
                cursor = db.query(TalkContract.ChatRooms.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CHAT_MEMBERS :
                cursor = db.query(TalkContract.ChatUserRooms.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default :
                throw new RuntimeException("Uri not matched");
        }
        if( cursor != null )
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri = null; // URI to be returned
        long id;
        switch (sUriMatcher.match(uri)) {
            case USERS :
                id = db.insert(TalkContract.User.TABLE_NAME, null, values);
                if( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(TalkContract.BASE_URI.buildUpon()
                                                                    .appendPath(TalkContract.User.FRIENDS_PATH).build(), id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case CHAT :
                id = db.insert(TalkContract.Chat.TABLE_NAME, null, values);
                if( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(TalkContract.BASE_URI.buildUpon()
                            .appendPath(TalkContract.Chat.PATH).build(), id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case CHAT_LIST :
                id = db.insert(TalkContract.ChatRooms.TABLE_NAME, null, values);
                if( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(TalkContract.BASE_URI.buildUpon()
                            .appendPath(TalkContract.Chat.PATH).build(), id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case CHAT_MEMBERS :
                id = db.insert(TalkContract.ChatUserRooms.TABLE_NAME, null, values);
                if( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(TalkContract.BASE_URI.buildUpon()
                            .appendPath(TalkContract.ChatUserRooms.PATH).build(), id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if( returnUri != null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int tasksDeleted; // starts as 0
        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {

            case USERS:
                tasksDeleted = db.delete(TalkContract.User.TABLE_NAME, null, null);
                break;
            // Handle USER_WITH_ID single item case, recognized by the ID included in the URI path
            case USER_WITH_ID:
                // Get the task ID from the URI path
                // Use selections/selectionArgs to filter for this ID
                tasksDeleted = db.delete(TalkContract.User.TABLE_NAME, selection, selectionArgs);
                break;
            case CHAT_LIST:
                tasksDeleted = db.delete(TalkContract.ChatRooms.TABLE_NAME, selection, selectionArgs);
                break;
            case CHAT_MEMBERS:
                tasksDeleted = db.delete(TalkContract.ChatUserRooms.TABLE_NAME, selection, selectionArgs);
                break;
            case CHAT :
                tasksDeleted = db.delete(TalkContract.Chat.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (tasksDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated = 0;
        switch( sUriMatcher.match(uri) ) {
            case USERS :
                rowUpdated = db.update(TalkContract.User.TABLE_NAME, values, selection, selectionArgs);

                break;
            case CHAT_ITEM :
                String Segment = uri.getLastPathSegment();
                rowUpdated = db.update(TalkContract.Chat.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CHAT_LIST :
                rowUpdated = db.update(TalkContract.ChatRooms.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CHAT :
                rowUpdated = db.update(TalkContract.Chat.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if ( rowUpdated != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case USERS :
                int insertedRow = 0;
                try {
                    db.beginTransaction();
                    for ( ContentValues value : values ) {
                        db.insert(TalkContract.User.TABLE_NAME, null, value);
                        insertedRow++;
                    }
                    db.setTransactionSuccessful();
                    if( insertedRow > 0 ) {
                        getContext().getContentResolver().notifyChange( uri, null );
                    }
                } catch ( Exception e ) {
                    Log.d(TAG, "bulkInsert: " + e);
                } finally {
                    db.endTransaction();
                }
                return insertedRow;
            default :
                return super.bulkInsert( uri, values );
        }
    }

    @Override
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }
}
