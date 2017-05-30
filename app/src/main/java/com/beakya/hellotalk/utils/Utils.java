package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
            out = new FileOutputStream(myPath);
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
        File root = c.getDir(iterator.next(), Context.MODE_PRIVATE);
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
            directory.delete();
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
        String myProfileDirectory = c.getString(R.string.setting_profile_img_directory);
        String FriendsFileDirectory = c.getString(R.string.setting_friends_img_directory);
        boolean r1 = deleteDirectory(c, Arrays.asList( myProfileDirectory ));
        boolean r2 = deleteDirectory(c, Arrays.asList( FriendsFileDirectory ));
        if( r1 && r2 ) {
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


    public static int insertMessage ( Context context, Message message, String chatId ) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues chatParams = new ContentValues();
        chatParams.put(TalkContract.ChatRooms.CHAT_ID, chatId);
        chatParams.put(TalkContract.Message.MESSAGE_ID, message.getMessageId());
        chatParams.put(TalkContract.Message.CREATOR_ID, message.getCreatorId());
        chatParams.put(TalkContract.Message.MESSAGE_CONTENT, message.getMessageContent());
        chatParams.put(TalkContract.Message.MESSAGE_TYPE, message.getMessageType());
        Uri insertedUri = resolver.insert(TalkContract.Message.CONTENT_URI, chatParams);
        return Integer.parseInt(insertedUri.getLastPathSegment());
    }
    public static int insertChatRoom ( ContentResolver resolver, ChatRoom chatRoom ) {
        ContentValues params = new ContentValues();
        params.put(TalkContract.ChatRooms.CHAT_ID, chatRoom.getChatId());
        params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, chatRoom.isSynchronized());
        params.put(TalkContract.ChatRooms.CHAT_ROOM_TYPE, chatRoom.getChatRoomType() );
        if( chatRoom.isSynchronized() ) {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 1);
        } else {
            params.put(TalkContract.ChatRooms.IS_SYNCHRONIZED, 0);
        }
        Uri insertedUri = resolver.insert(TalkContract.ChatRooms.CONTENT_URI, params);
        return Integer.parseInt(insertedUri.getLastPathSegment());
    }
    public static int addChatIdIntoUser ( Context context, String chatId, String userId ) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(TalkContract.ChatRooms.CHAT_ID, chatId );
        return resolver.update(TalkContract.User.CONTENT_URI, values, TalkContract.User.USER_ID + " = ?" , new String[] { userId } );
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
    public static void ChatInitialize( Context context, ChatRoom chatRoom ) {
        ContentResolver resolver = context.getContentResolver();
        if ( chatRoom.getChatRoomType() == 1 ) {
            int userListSize = chatRoom.getUserList().size();
            if ( userListSize == 1 ) {
                for ( HashMap.Entry<String, User> entry : chatRoom.getUserList().entrySet() ) {
                    User user = entry.getValue();
                    addChatIdIntoUser(context, chatRoom.getChatId(), user.getId());
                }
            }
        }
        insertChatRoom(resolver, chatRoom);
        insertChatMembers(resolver, chatRoom.getChatId(), chatRoom.getUserList());
    }

    public static ArrayList<User> JSONArrayToArrayList(JSONArray json ) throws JSONException {
        ArrayList<User> result = new ArrayList<>();
        for ( int i = 0; i < json.length(); i++ ) {
            result.add(new User(json.getString(i),null));
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
        Date result = null;
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        try {
            result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.setTime(result.getTime() + (hour * 9));
        long diff = Math.abs(currentDate.getTime() - result.getTime());

        if (diff < minute) {
            long time = diff / second;
            return time + "초전";
        } else if (diff < hour) {
            long time = diff / minute;
            return time + "분전";
        } else if (diff < day) {
            return (diff / hour) + "시간전";
        } else if ( diff > day ){
            //TODO 3월 15일 이런식으로 리턴하게 만들것
            return null;
        } else {
            return "방금";
        }
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

    public static ChatRoom extractChatRoomFromJson ( JSONObject object ) throws JSONException {
        HashMap<String, User> users = new HashMap<>();
        JSONArray array = object.getJSONArray("members");
        String chatId = object.getString(TalkContract.ChatRooms.CHAT_ID);
        int chatType = object.getInt(TalkContract.ChatRooms.CHAT_ROOM_TYPE);
        boolean isSynchronized = true;
        for( int i = 0; i < array.length(); i++ ) {
            JSONObject userObj = array.getJSONObject(i);
            String id = userObj.getString(TalkContract.User.USER_ID);
            String name = userObj.getString(TalkContract.User.USER_NAME);
            users.put(id, new User(id, name));
        }
        return new ChatRoom(users, chatId, chatType, isSynchronized);
    }
    public static Message extractMessageFromJson ( JSONObject object) throws JSONException {
        int messageType = object.getInt(TalkContract.Message.MESSAGE_TYPE);
        String creatorId = object.getString(TalkContract.Message.CREATOR_ID);
        String messageContent = object.getString(TalkContract.Message.MESSAGE_CONTENT);
        String chatId = object.getString(TalkContract.ChatRooms.CHAT_ID);
        String messageId = object.getString(TalkContract.Message.MESSAGE_ID);

        return new Message(messageId, creatorId, messageContent, chatId, messageType, 0);
    }
}
