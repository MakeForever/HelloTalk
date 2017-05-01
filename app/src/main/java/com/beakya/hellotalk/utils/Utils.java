package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.DbHelper;
import com.beakya.hellotalk.database.TalkContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 4. 8..
 */

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();
    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.user_info), MODE_PRIVATE);
        String token = preferences.getString(context.getString(R.string.token), null);
        return token;
    }
    public static boolean checkToken( Context context ) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.user_info), MODE_PRIVATE);
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
            return null;
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
        boolean imageDeleteResult = dropAllProfileImg(c);
        boolean dropTableResult = dropChatListTables(c);
        if ( userDeletedRow != -1 && dropTableResult && imageDeleteResult ) {
            SharedPreferences storage = c.getSharedPreferences(c.getString(R.string.user_info), MODE_PRIVATE);
            boolean result = storage.edit().clear().commit();

            Log.d(TAG, "logout: " + userDeletedRow + " : " + dropTableResult+ " : " + result + " : " + imageDeleteResult);

            return result;
        }
        return false;
    }
    public static boolean dropChatListTables( Context c ) {
        String dropSqlStart = "DROP TABLE IF EXISTS ' ";
        String dropSqlEnd = " ' ";
        DbHelper helper = new DbHelper(c);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentResolver resolver = c.getContentResolver();
        try {
            Cursor cursor = resolver.query(TalkContract.ChatList.CONTENT_URI, new String[]{ TalkContract.ChatList.CHAT_LIST_ID }, null, null, null);
            while( cursor.moveToNext() ) {
                db.execSQL(dropSqlStart + cursor.getString(cursor.getColumnIndex(TalkContract.ChatList.CHAT_LIST_ID)) + dropSqlEnd);
            }
            int result = resolver.delete(TalkContract.ChatList.CONTENT_URI, null, null);
            if( result == -1 ) {

            }
            return true;
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
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
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
}
