package com.beakya.hellotalk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.beakya.hellotalk.utils.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

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
//        value.put( TalkContract.User.USER_ID, "beak_ya@naver.com" );
//        value.put( TalkContract.User.USER_NAME, "박철호" );
//        value.put( TalkContract.User.USER_HAVE_PROFILE_IMAGE, byteArray );
//        ContentValues[] result = new ContentValues[1];
//        result = values.toArray(result);
//        int insertResult = appContext.getContentResolver().bulkInsert(
//                Uri.parse(TalkContract.BASE_URI + "/" + TalkContract.User.FRIENDS_PATH), result );
//
//        assertEquals("not inserted",1,insertResult);

    }
}
