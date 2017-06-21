package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 4. 8..
 */

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();
    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        String token = preferences.getString(context.getString(R.string.token), null);
        return token;
    }
    public static boolean checkToken( Context context ) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        String token = preferences.getString(context.getString(R.string.token), null);
        if( token == null ) {
            return false;
        } else {
            return true;
        }
    }
    public static MyApp getMyApp(Context context ) {
        MyApp app = (MyApp) context.getApplicationContext();
        return app;
    }

    public static String saveToInternalStorage( Context c, Bitmap bitmap, String name , String extension,  List<String> directories ) {

//        File directory = c.getDir(de, Context.MODE_PRIVATE);
        File path = generateDirectory(c, directories);
        File myPath = new File( path, name + "." + extension );
        path.mkdirs();
        FileOutputStream out;
        try {
            myPath.createNewFile();
            out = new FileOutputStream(myPath, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myPath.getAbsolutePath();
    }
    public static File generateDirectory(Context c, List<String> directories) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = directories.iterator();
        File root = c.getFilesDir();
        while(iterator.hasNext()) {
            builder.append(iterator.next());
            if(iterator.hasNext()) {
                builder.append("/");
            }
        }
        File target = new File( root, builder.toString() );
        return target;
    }

    public static Bitmap getImageBitmap(Context c, String name, String extension, List<String> directories){
        Bitmap b;
        File directory = generateDirectory(c, directories);
        File f = new File(directory, name + "." + extension);
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            return BitmapFactory.decodeResource(c.getResources(),R.mipmap.default_profile_img);
        }
        return b;
    }
    public static boolean deleteDirectory( Context c, List<String> directories ) {
        File directory = generateDirectory(c, directories);
        boolean result = false;
        try {

            String[] children = directory.list();
            if ( children != null ) {
                for ( String s : children ) {
                    File file = new File ( directory, s );
                    String[] secondDir = file.list();
                    for ( String t : secondDir ) {
                        File k = new File( file.getPath(), t);
                        k.delete();
                    }
                    file.delete();
                }
                boolean deleted = directory.delete();
            }
            result = true;
        } catch ( Exception e ){
            e.printStackTrace();
        } finally {
            return result;
        }
    }
    public static boolean deleteFile(Context c , String name, String extension, List<String> directories) {
        File directory = generateDirectory(c, directories);
        boolean result = false;
        try {
            File f = new File(directory, name + "." + extension);
            result = f.delete();
        } catch( Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static Bitmap decodeImgStringBase64(String encoded) {
        if( encoded != null ) {
            byte[] imageBytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }

    public static boolean logout( Context c ){
        ContentResolver resolver = c.getContentResolver();
        int userDeletedRow = resolver.delete(TalkContract.User.CONTENT_URI, null, null);
        int chatDeletedRow = resolver.delete( TalkContract.Message.CONTENT_URI, null, null );
        int chatListDeletedRow = resolver.delete( TalkContract.ChatRooms.CONTENT_URI, null, null );
        int chatMembersDeleteRow = resolver.delete(TalkContract.ChatUserRooms.CONTENT_URI, null, null);
        boolean imageDeleteResult = dropAllProfileImg(c);
        if ( userDeletedRow != -1 && imageDeleteResult ) {
            SharedPreferences storage = c.getSharedPreferences(c.getString(R.string.my_info), MODE_PRIVATE);
            boolean result = storage.edit().clear().commit();

            Log.d(TAG, "userDeletedRow : " + userDeletedRow + " chatDeletedRow : " + chatDeletedRow + " chatListDeletedRow : " + chatListDeletedRow + " chatMembersDeleteRow : " + chatMembersDeleteRow);

            return result;
        }
        return false;
    }

    public static boolean dropAllProfileImg( Context c ) {
        String FriendsFileDirectory = c.getString(R.string.setting_friends_img_directory);
        boolean r2 = deleteDirectory(c, Arrays.asList( FriendsFileDirectory ));
        if( r2 ) {
            return true;
        } else
            return false;
    }
    public static String hashFunction(String base, String hashingType) {
        try{
            MessageDigest digest = MessageDigest.getInstance(hashingType);
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static ContentValues bundleToContentValues( Bundle bundle ) {
        ContentValues returnValue = new ContentValues();
        if( bundle != null ) {
            for(String key : bundle.keySet()) {
                Object object = bundle.get(key);
                Log.d(TAG, "bundleToContentValues: " + object.getClass().getName());
                switch( object.getClass().getName() ) {
                    case "java.lang.String" :
                        returnValue.put( key, ( String ) object );
                        break;
                }
            }
        }
        return returnValue;
    }


    public static int insertMessage (Context context, Message stringMessage, String chatId, boolean isRead ) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues chatParams = new ContentValues();
        chatParams.put(TalkContract.ChatRooms.CHAT_ID, chatId);
        chatParams.put(TalkContract.Message.MESSAGE_ID, stringMessage.getMessageId());
        chatParams.put(TalkContract.Message.CREATOR_ID, stringMessage.getCreatorId());
        chatParams.put(TalkContract.Message.MESSAGE_CONTENT, stringMessage.getMessageContent());
        chatParams.put(TalkContract.Message.MESSAGE_TYPE, stringMessage.getMessageType());
        chatParams.put(TalkContract.Message.READING_COUNT, stringMessage.isReadCount());
        chatParams.put(TalkContract.Message.CREATED_TIME, stringMessage.getCreatedTime());
        if ( isRead ){
            chatParams.put(TalkContract.Message.IS_READ, 1);
        } else {
            chatParams.put(TalkContract.Message.IS_READ, 0);
        }
        Uri insertedUri = resolver.insert(TalkContract.Message.CONTENT_URI, chatParams);
        return Integer.parseInt(insertedUri.getLastPathSegment());
    }


    public static void ChatInitialize( Context context, ChatRoom chatRoom) {
        ContentResolver resolver = context.getContentResolver();
        if( chatRoom instanceof PersonalChatRoom ) {
            PersonalChatRoom personalChatRoom = (PersonalChatRoom) chatRoom;
            insertChatRoom(resolver, personalChatRoom);
            insertChatMembers(resolver, personalChatRoom.getChatId(), Arrays.asList(new User[] { personalChatRoom.getTalkTo() }));
        } else if ( chatRoom instanceof  GroupChatRoom ) {
            SharedPreferences tokenStorage = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
            String myId = tokenStorage.getString(context.getString(R.string.user_id), null);
            GroupChatRoom groupChatRoom = (GroupChatRoom) chatRoom;
            insertChatRoom(resolver, groupChatRoom);
            insertChatMembers(resolver, groupChatRoom.getChatId(), groupChatRoom.getUsers());
            for( User user : groupChatRoom.getUsers().values() ) {
                if ( !myId.equals( user.getId())) {
                    insertUser(context, user);
                }
            }
        }
    }

    public static int insertChatRoom ( ContentResolver resolver, PersonalChatRoom chatRoom) {
        ContentValues params = new ContentValues();
        params.put(TalkContract.ChatRooms.CHAT_ID, chatRoom.getChatId());
        params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, chatRoom.isSynchronized());
        params.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, chatRoom.getChatRoomType());
        params.put(TalkContract.ChatRooms.CREATED_TIME, Utils.getCurrentTime());
        if( chatRoom.isSynchronized() ) {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 1);
        } else {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 0);
        }
        Uri insertedUri = resolver.insert(TalkContract.ChatRooms.CONTENT_URI, params);
        return Integer.parseInt(insertedUri.getLastPathSegment());
    }
    public static int insertChatRoom ( ContentResolver resolver, GroupChatRoom chatRoom) {
        ContentValues params = new ContentValues();
        params.put(TalkContract.ChatRooms.CHAT_ID, chatRoom.getChatId());
        params.put(TalkContract.ChatRooms.CHAT_NAME, chatRoom.getChatName());
        params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, chatRoom.isSynchronized());
        params.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, chatRoom.getChatRoomType());
        params.put(TalkContract.ChatRooms.CREATED_TIME, Utils.getCurrentTime());
        if( chatRoom.isSynchronized() ) {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 1);
        } else {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 0);
        }
        Uri insertedUri = resolver.insert(TalkContract.ChatRooms.CONTENT_URI, params);
        return Integer.parseInt(insertedUri.getLastPathSegment());
    }

    //TODO : 나중에 bulkInsert 로 바꾸어야 한다
    public static void insertChatMembers ( ContentResolver resolver, String chatId, HashMap<String, User> users ) {
        ArrayList<ContentValues> chatMemberContentValues = new ArrayList<ContentValues>();
        for( User user : users.values() ) {
            ContentValues params = new ContentValues();
            params.put(TalkContract.ChatRooms.CHAT_ID, chatId);
            params.put(TalkContract.User.USER_ID, user.getId());
            chatMemberContentValues.add( params );
        }
        for( ContentValues value : chatMemberContentValues ) {
            resolver.insert(TalkContract.ChatUserRooms.CONTENT_URI, value);
        }
    }
    public static void insertUser( Context c, User user ) {
        DbHelper dbHelper = new DbHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                TalkContract.User.TABLE_NAME,
                null,
                TalkContract.User.USER_ID + " = ?",
                new String[] { user.getId() },
                null,
                null,
                null
                );
        if (cursor.getCount() == 0 ) {
            ContentValues values = new ContentValues();
            values.put(TalkContract.User.USER_ID, user.getId());
            values.put(TalkContract.User.USER_NAME, user.getName());
            if( user.hasProfileImg() && user.getProfileImage() != null ) {
                values.put(TalkContract.User.HAVE_PROFILE_IMAGE, 1);
                saveToInternalStorage(c, user.getProfileImage(),
                        c.getString(R.string.setting_friends_profile_img_name),
                        c.getString(R.string.setting_profile_img_extension),
                        Arrays.asList( new String[]{ c.getString(R.string.setting_friends_img_directory), user.getId() }));
            }
            db.insert(TalkContract.User.TABLE_NAME, null, values);
        }

    }
    public static User findUser ( Context context, String userId ) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ?",
                new String[] { userId },
                null);

        if ( cursor.getCount() == 0 ) {
            return null;
        } else {
            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndex(TalkContract.User.USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(TalkContract.User.USER_NAME));
            boolean hasPic = cursor.getInt(cursor.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0;
            return new User ( id, name, hasPic);
        }
    }
    public static void insertChatMembers ( ContentResolver resolver, String chatId, List<User> users ) {
        ArrayList<ContentValues> chatMemberContentValues = new ArrayList<ContentValues>();
        for( User user : users ) {
            ContentValues params = new ContentValues();
            params.put(TalkContract.ChatRooms.CHAT_ID, chatId);
            params.put(TalkContract.User.USER_ID, user.getId());
            chatMemberContentValues.add( params );
        }
        for( ContentValues value : chatMemberContentValues ) {
            resolver.insert(TalkContract.ChatUserRooms.CONTENT_URI, value);
        }
    }


    public static ArrayList<String> JSONArrayToArrayList(JSONArray json, String name ) throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        for ( int i = 0; i < json.length(); i++ ) {
            JSONObject k = json.getJSONObject(i);
            result.add(k.getString(name));
        }
        return result;
    }

    public static String ChatTableNameCreator(List<String> list ) {
        StringBuilder builder = new StringBuilder();
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        for(String value : list) {
            Log.d(TAG, "ChatTableNameCreator: " + value);
            builder.append(value);
        }
        return hashFunction(builder.toString(), "SHA-256");
    }


    public static String timeToString( String date ) {

        long second = 1000;
        long minute = 1000 * 60;
        long hour = minute * 60;
        long day = hour * 24;
        Date resultTime = null;
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        try {
            resultTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        resultTime.setTime(resultTime.getTime());
        long diff = Math.abs(currentDate.getTime() - resultTime.getTime());
        StringBuilder builder = new StringBuilder();
        calendar.setTime(resultTime);
        if (diff < day) {
            int i = calendar.get(Calendar.AM_PM);
            if ( i == 1 ){
                builder.append("오후 ");
            } else {
                builder.append("오전 ");
            }
            builder.append(calendar.get(Calendar.HOUR)+ "시");
            builder.append(calendar.get(Calendar.MINUTE) + "분");
        } else {

        }
        return builder.toString();
    }


    public static String getUserChatId (Context context, String userId ) {
        String chatId = null;
        Cursor cursor = context.getContentResolver().query(
                TalkContract.User.CONTENT_URI,
                new String[] { TalkContract.ChatRooms.CHAT_ID },
                TalkContract.User.USER_ID + " = ? ",
                new String[] { userId },
                null);

        while(cursor.moveToNext()) {
            chatId = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_ID));
        }

        return ( chatId == null ) ?  hashFunction(userId + System.currentTimeMillis(), "SHA-256") :  chatId;
    }

    public static GroupChatRoom extractChatRoomFromJson (JSONObject object ) throws JSONException {
        HashMap<String, User> users = new HashMap<>();
        JSONArray array = object.getJSONArray("members");
        String chatName = object.getString(TalkContract.ChatRooms.CHAT_NAME);
        String chatId = object.getString(TalkContract.ChatRooms.CHAT_ID);
        int chatType = object.getInt(TalkContract.ChatRooms.CHAT_ROOM_TYPE);
        boolean isSynchronized = true;
        for( int i = 0; i < array.length(); i++ ) {
            JSONObject userObj = array.getJSONObject(i);
            String id = userObj.getString(TalkContract.User.USER_ID);
            String name = userObj.getString(TalkContract.User.USER_NAME);
            users.put(id, new User(id, name, true));
        }
        return new GroupChatRoom(chatName, users, chatId, chatType, isSynchronized);
    }
    public static PersonalChatRoom extractPersonalChatRoomFromJson (JSONObject object, User user ) throws JSONException {
        String chatId = object.getString(TalkContract.ChatRooms.CHAT_ID);
        int chatType = object.getInt(TalkContract.ChatRooms.CHAT_ROOM_TYPE);
        boolean isSynchronized = true;
        return new PersonalChatRoom(chatId, chatType, isSynchronized, user );
    }
    public static GroupChatRoom extractGroupChatRoomFromJson (JSONObject object, Context context ) throws JSONException {
        String chatId = object.getString(TalkContract.ChatRooms.CHAT_ID);
        int chatType = object.getInt(TalkContract.ChatRooms.CHAT_ROOM_TYPE);
        boolean isSynchronized = true;
        return null;
    }

    public static User extractUserFromJson ( JSONObject object ) throws JSONException {
        return new User (object.getString(TalkContract.User.USER_ID), object.getString(TalkContract.User.USER_NAME), true);
    }
    public static String createIsReadMessageJsonObj ( String event, String chatId, List<String> list , User user) {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            object.put("from", user.getId());
            object.put(TalkContract.ChatRooms.CHAT_ID, chatId);
            for( int i = 0 ; i < list.size(); i++ ) {
                JSONObject arrayItem = new JSONObject();
                arrayItem.put(TalkContract.Message.MESSAGE_ID, list.get(i));
                array.put(i, arrayItem);
            }
            object.put("message_id_list", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }

    public static User getMyInfo ( Context context ) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        String myId = preferences.getString(context.getString(R.string.user_id), null);
        String myName = preferences.getString(context.getString(R.string.user_name), null);
        boolean hasPic = preferences.getBoolean(context.getString(R.string.user_img_boolean), true);
        return new User(myId, myName, hasPic);
    }
    public static String groupChatReadObjCreator (int chatType, User sender, String chatId, List<String> messages ) {
        Gson gson = new Gson();
        JsonElement msgElement = gson.toJsonTree(messages);
        JsonObject object = new JsonObject();
        object.addProperty(TalkContract.ChatRooms.CHAT_ID, chatId);
        object.add("messages", msgElement);
        object.addProperty("event", "chat_read");
        object.addProperty("sender", sender.getId());
        object.addProperty("chatType", chatType);
        return object.toString();
    }
    public static String personalChatReadObjCreator(int chatType, User sender, User receiver, String chatId, ArrayList<String> messages ) {
        Gson gson = new Gson();
        JsonElement msgElement = gson.toJsonTree(messages);
        JsonObject object = new JsonObject();
        object.addProperty(TalkContract.ChatRooms.CHAT_ID, chatId);
        object.addProperty("receiver", receiver.getId());
        object.add("messages", msgElement);
        object.addProperty("event", "chat_read");
        object.addProperty("sender", sender.getId());
        object.addProperty("chatType", chatType);
        return object.toString();
    }
    public static boolean checkUserInDb ( Context context, String userId ) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ? ",
                new String[] { userId },
                null);

        if( cursor.getCount() > 0 )
            return true;
        else return false;
    }
}
