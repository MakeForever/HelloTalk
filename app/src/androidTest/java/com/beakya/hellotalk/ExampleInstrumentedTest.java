package com.beakya.hellotalk;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static final String TAG = ExampleInstrumentedTest.class.getCanonicalName();
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
//        List<ContentValues> values = new ArrayList<>();
//        ContentValues value = new ContentValues();
//        String url = "http://icons.iconarchive.com/icons/cornmanthe3rd/metronome/128/Internet-chrome-icon.png";
//
//        InputStream in = new URL(url).openConnection().getInputStream();
//        Bitmap bitmap = BitmapFactory.decodeStream(in);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress( Bitmap.CompressFormat.PNG, 100, stream ) ;
//        byte[] byteArray = stream.toByteArray() ;
//
//        value.put( TalkContract.Friend.USER_ID, "beak_ya@naver.com" );
//        value.put( TalkContract.Friend.USER_NAME, "박철호" );
//        value.put( TalkContract.Friend.USER_PROFILE_IMAGE_PATH, byteArray );
//        ContentValues[] result = new ContentValues[1];
//        result = values.toArray(result);
//        int insertResult = appContext.getContentResolver().bulkInsert(
//                Uri.parse(TalkContract.BASE_URI + "/" + TalkContract.Friend.FRIENDS_PATH), result );
//
//        assertEquals("not inserted",1,insertResult);
        File file = Utils.generateDirectory(appContext, Arrays.asList(new String[] {"test" ,"alpha", "beta"}));
        Log.d(TAG, "useAppContext: " + file.getAbsolutePath());
    }
}
