package com.beakya.hellotalk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cheolho on 2017. 4. 8..
 */

public class Utils {
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
        Bitmap b = null;
        File directory = generateDirectory(c, directories);
        File f = new File(directory, name + "." + extension);
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
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
}
