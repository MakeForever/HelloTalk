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
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.google.firebase.iid.FirebaseInstanceId;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 4. 8..
 */

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();
    public static long second = 1000;
    public static long minute = second * 60;
    public static long hour = minute * 60;
    public static long day = hour * 24;
    public static long year = day * 365;
    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        String token = preferences.getString(context.getString(R.string.token), null);
        return token;
    }
    public static boolean checkToken( Context context ) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.my_info), MODE_PRIVATE);
        String token = preferences.getString(context.getString(R.string.token), null);
        return token != null;
    }
    public static MyApp getMyApp(Context context ) {
        MyApp app = (MyApp) context.getApplicationContext();
        return app;
    }
    public static Bitmap resizeBitmapImage(Bitmap source, int maxResolution)
    {
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if(width > height)
        {
            if(maxResolution < width)
            {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        }
        else
        {
            if(maxResolution < height)
            {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
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
    public static void setMyProfileImage(Context c, Bitmap bitmap ) {
        String fileName = c.getString(R.string.setting_friends_profile_img_name);
        String extension = c.getString(R.string.setting_profile_img_extension);
        String directory = c.getString(R.string.setting_friends_img_directory);
        User myInfo = getMyInfo(c);
        saveToInternalStorage(c, bitmap, fileName, extension, Arrays.asList(directory, myInfo.getId()));
    }
    public static void setFriendProfileImage(Context mContext, Bitmap bitmap, String id) {
        saveToInternalStorage(mContext, bitmap,
                mContext.getString(R.string.setting_friends_profile_img_name),
                mContext.getString(R.string.setting_profile_img_extension),
                Arrays.asList(mContext.getString(R.string.setting_friends_img_directory), id));
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
    public static GroupChatRoom getGroupChatRoom ( String chatId, Context c ) {
        DbHelper dbHelper = new DbHelper(c);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TalkContract.ChatRooms.TABLE_NAME,
                null,
                TalkContract.ChatRooms.CHAT_ID + " = ? ",
                new String[] { chatId },
                null,
                null,
                null);
        GroupChatRoom chatRoom = null;

        while ( cursor.moveToNext() ) {
            String chatName = cursor.getString(cursor.getColumnIndex(TalkContract.ChatRooms.CHAT_NAME));
            String roomUsersQuery = " SELECT " + " b.* "+" FROM " + TalkContract.ChatRoomUsers.TABLE_NAME + " as a "+
                    " INNER JOIN " + TalkContract.User.TABLE_NAME + " as b ON " + " a."+ TalkContract.User.USER_ID + " = " + " b." + TalkContract.User.USER_ID + " WHERE " +
                    "a."+ TalkContract.ChatRooms.CHAT_ID + " = " +"'"+ chatId+"'";

            Log.d(TAG, "loadInBackground roomUsersQuery :" + roomUsersQuery);
            Cursor roomUsers = db.rawQuery(roomUsersQuery, null);
            HashMap<String, User> userList = new HashMap<>();
            while( roomUsers.moveToNext() ) {
                userList.put(
                        roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID))
                        ,new User(
                                roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_ID)),
                                roomUsers.getString(roomUsers.getColumnIndex(TalkContract.User.USER_NAME)),
                                roomUsers.getInt(roomUsers.getColumnIndex(TalkContract.User.HAVE_PROFILE_IMAGE)) > 0
                        )
                );
            }
            chatRoom = new GroupChatRoom(chatName, userList, chatId, 2, true);
        }
        return chatRoom;
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
        int chatMembersDeleteRow = resolver.delete(TalkContract.ChatRoomUsers.CONTENT_URI, null, null);
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
        return deleteDirectory(c, Arrays.asList( FriendsFileDirectory ));
    }
    public static String hashFunction(String base, String hashingType) {
        try{
            MessageDigest digest = MessageDigest.getInstance(hashingType);
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
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
        chatParams.put(TalkContract.Message.READING_COUNT, stringMessage.getReadCount());
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

    public static void deleteChatRoom(Context context, String chatId ) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentResolver resolver = context.getContentResolver();
        String userIdCol = TalkContract.User.USER_ID;
        String chatIdCol = TalkContract.ChatRooms.CHAT_ID;
        String chatMembersCol = TalkContract.ChatRoomUsers.TABLE_NAME;

        String query = " select cm1." + userIdCol +" from " + chatMembersCol + " as cm1 " + " where " + " cm1." + chatIdCol + " = " +" '" + chatId+ "' " +
                " and (select count(*) as count from " + chatMembersCol + " as cm2 " + " where " + "cm2." + userIdCol + " = " + "cm1." + userIdCol  + " ) < 2" +
                " and ( select count(*) as count from " + TalkContract.User.TABLE_NAME + " as us " + " where us." + TalkContract.User.IS_MY_FRIEND + " = 0 and " + "us." + userIdCol + " = cm1." + userIdCol + " ) > 0 " ;

        Cursor cursor = null;
        try {
            db.beginTransaction();
            cursor = db.rawQuery(query, null);
            //채팅 맴버중 삭제 가능한 맴버 삭제
            while( cursor.moveToNext() ) {
                String id = cursor.getString(cursor.getColumnIndex(TalkContract.User.USER_ID));
                db.delete(
                        TalkContract.User.TABLE_NAME,
                        TalkContract.User.USER_ID + " = ? ",
                        new String[] { id }
                );
                // 프로필 이미지 삭제
                Utils.deleteFile(context, id, "png", Arrays.asList("users", id));
            }

            // 메세지 삭제
            db.delete(
                TalkContract.Message.TABLE_NAME,
                TalkContract.ChatRooms.CHAT_ID + " = ? ",
                new String[] { chatId }
            );
            //채팅 맴버 삭제
            db.delete(
                TalkContract.ChatRoomUsers.TABLE_NAME,
                TalkContract.ChatRooms.CHAT_ID + " = ? ",
                new String[] { chatId }
            );
            // 채팅룸 삭제 삭제
            db.delete(
                    TalkContract.ChatRooms.TABLE_NAME,
                    TalkContract.ChatRooms.CHAT_ID + " = ? ",
                    new String[] { chatId }
            );
            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
            db.close();
        }


//        Log.d(TAG, "deleteChatRoom: deleteChatRoomColumns" + deleteChatRoomResult + " deletedMessagesCount : " + deleteMessagesResult );
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
            resolver.insert(TalkContract.ChatRoomUsers.CONTENT_URI, value);
        }
    }
    public static void insertUser( Context c, User user ) {
        DbHelper dbHelper = new DbHelper(c);
        ContentResolver resolver = c.getContentResolver();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = resolver.query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ?",
                new String[] { user.getId() },
                null
        );

        if (cursor.getCount() == 0 ) {
            ContentValues values = new ContentValues();
            values.put(TalkContract.User.USER_ID, user.getId());
            values.put(TalkContract.User.USER_NAME, user.getName());
            values.put(TalkContract.User.IS_MY_FRIEND, user.isMyFriend());
            values.put(TalkContract.User.HAVE_PROFILE_IMAGE, user.hasProfileImg());
            if( user.hasProfileImg() && user.getProfileImage() != null ) {
                values.put(TalkContract.User.HAVE_PROFILE_IMAGE, 1);
                saveToInternalStorage(c, user.getProfileImage(),
                        c.getString(R.string.setting_friends_profile_img_name),
                        c.getString(R.string.setting_profile_img_extension),
                        Arrays.asList(c.getString(R.string.setting_friends_img_directory), user.getId()));
            }
            resolver.insert(TalkContract.User.CONTENT_URI, values);
        }
        cursor.close();

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
            resolver.insert(TalkContract.ChatRoomUsers.CONTENT_URI, value);
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


    public static String changeMessageString(String date ) {

        Calendar targetTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        try {
            targetTime.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = Math.abs(currentTime.getTime().getTime() - targetTime.getTime().getTime());
        int currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        int targetDay = targetTime.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        if ( currentDay == targetDay ) {
            int i = targetTime.get(Calendar.AM_PM);
            if ( i == 1 ){
                builder.append("오후 ");
            } else {
                builder.append("오전 ");
            }
            builder.append(targetTime.get(Calendar.HOUR)+ "시");
            builder.append(" ");
            builder.append(targetTime.get(Calendar.MINUTE) + "분");
        } else {
            builder.append((targetTime.get(Calendar.MONTH) + 1) + "월");
            builder.append(" ");
            builder.append(targetTime.get(Calendar.DAY_OF_MONTH));
            builder.append("일");
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

        return cursor.getCount() > 0;
    }
    public static IO.Options getOptions( Context context ) {
        int timeoutLimit = 5000;
        IO.Options options = new IO.Options();
        String token = Utils.getToken(context);
        String fireBaseToken = FirebaseInstanceId.getInstance().getToken();
        options.query = "jwt_token=" + token +"&" + "fire_base_token=" + fireBaseToken;
        options.timeout = timeoutLimit;
        return options;
    }
    public static IO.Options getTemporaryOptions ( Context context ) {
        int timeoutLimit = 5000;
        IO.Options options = new IO.Options();
        String token = Utils.getToken(context);
        options.query = "jwt_token=" + token + "&" + "isTemporary=" + true;
        options.timeout = timeoutLimit;
        return options;
    }
    public static void updateChatRoomUsers (Context context, String chatId, String userId, boolean flag ) {
        ContentValues values = new ContentValues();
        values.put(TalkContract.ChatRoomUsers.IS_MEMBER, flag ? 1 : 0 );
        context.getContentResolver().update(
                TalkContract.ChatRoomUsers.CONTENT_URI,
                values,
                TalkContract.User.USER_ID + " = ? and " + TalkContract.ChatRooms.CHAT_ID + " = ? ",
                new String[] { userId, chatId }
        );
    }
    public static void showInAppNotification(Context mContext, @NonNull Message message ) {

    }
}
