package com.beakya.hellotalk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by goodlife on 2017. 8. 3..
 */

public class PopUpNotificationManager {
    public static final String TAG = PopUpNotificationManager.class.getSimpleName();
    private int sleepTime = 2000;
    private Toast mToast;
    private Handler handler = new Handler();
    private boolean isToastShow = false;
    private CircleImageView profileIamgeView;
    private TextView nameTextView;
    private TextView contentTextView;
    private Context mContext;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mToast.cancel();
            isToastShow = false;
        }
    };

    private static class Singleton {
       private static PopUpNotificationManager instance = new PopUpNotificationManager();
    }
    public static PopUpNotificationManager getInstance() {
        return Singleton.instance;
    }
    public synchronized void show ( Message message ) {
        if ( isToastShow ) {
            update(message);
        } else {
            isToastShow = true;
            handler.postDelayed(runnable, sleepTime);
        }
        User user = Utils.findUser(mContext, message.getCreatorId());
        contentTextView.setText(message.getMessageContent());
        if (user != null) {
            nameTextView.setText(user.getName());
            profileIamgeView.setImageBitmap(user.getProfileImg(mContext));
        }
//        Log.d(TAG, "show: " + mToast.hashCode());
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if ( !sharedPreferences.getBoolean("is_vibrate", false) ) {
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, defaultSoundUri);
            ringtone.play();
        } else {
            Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(500);
        }
        mToast.show();
    }
    private void update( Message message ) {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, sleepTime);
    }
    public void setToast(Context context, Toast mToast) {
        this.mContext = context;
        this.mToast = mToast;
        View view = mToast.getView();
        profileIamgeView = (CircleImageView) view.findViewById(R.id.circleImageView);
        contentTextView = (TextView) view.findViewById(R.id.content_text_view);
        nameTextView = (TextView) view.findViewById(R.id.name_text_view);
    }
    public boolean isPopupWindowNull () {
        return mToast == null;
    }
}
