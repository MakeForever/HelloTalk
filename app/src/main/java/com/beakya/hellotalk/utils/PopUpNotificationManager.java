package com.beakya.hellotalk.utils;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.Message;

/**
 * Created by goodlife on 2017. 8. 3..
 */

public class PopUpNotificationManager {
    public static final String TAG = PopUpNotificationManager.class.getSimpleName();
    private int sleepTime = 2000;
    private PopupWindow popupWindow;
    private Thread timer = new Thread( new Runnable() {
        @Override
        public void run() {
            try {
//                Thread.currentThread().sleep(sleepTime);
                this.wait(sleepTime);
                if ( popupWindow.isShowing() ) {
                    Log.d(TAG, "popupWindow dismiss");
                    popupWindow.dismiss();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "run: interrupted");
            }
        }
    });
    private static class Singleton {
       private static PopUpNotificationManager manager = new PopUpNotificationManager();
    }
    public static PopUpNotificationManager getInstance() {
        return Singleton.manager;
    }
    public void show(View targetView, Message message ) {
        if ( popupWindow.isShowing() ) {
            update(message);
        } else {
            popupWindow.setAnimationStyle(R.style.animationName);
            popupWindow.showAtLocation(targetView, Gravity.TOP, 0, 200);
        }
        timer.run();
    }
    private void update( Message message ) {
        if ( timer.isAlive() ) {
            timer.interrupt();
        }
    }
    public void setPopupWindow ( PopupWindow popupWindow ) {
        this.popupWindow = popupWindow;
    }
    public boolean isPopupWindowNull () {
        return popupWindow == null;
    }
}
