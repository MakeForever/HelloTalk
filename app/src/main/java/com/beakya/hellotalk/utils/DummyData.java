package com.beakya.hellotalk.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.beakya.hellotalk.database.TalkContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheolho on 2017. 3. 26..
 */

public class DummyData {

    public static void addDummyData(final Context context ) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<ContentValues> values = new ArrayList<>();
                ContentValues value = new ContentValues();
                String url = "http://icons.iconarchive.com/icons/cornmanthe3rd/metronome/128/Internet-chrome-icon.png";

                InputStream in = null;
                try {
                    in = new URL(url).openConnection().getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(in);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress( Bitmap.CompressFormat.PNG, 100, stream ) ;
                byte[] byteArray = stream.toByteArray() ;

                value.put( TalkContract.Friend.USER_ID, "beak_ya@naver.com" );
                value.put( TalkContract.Friend.USER_NAME, "박철호" );
//                value.put( TalkContract.Friend.USER_PROFILE_IMAGE_PATH, byteArray );
                values.add(value);
                ContentValues[] result = new ContentValues[1];
                result = values.toArray(result);
                int insertResult = context.getContentResolver().bulkInsert(
                        Uri.parse(TalkContract.BASE_URI + "/" + TalkContract.Friend.FRIENDS_PATH), result );
                return null;
            }
        }.execute();

    }

}
