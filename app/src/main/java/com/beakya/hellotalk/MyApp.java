package com.beakya.hellotalk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketCreator;
import com.beakya.hellotalk.utils.SocketTask;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

import static com.beakya.hellotalk.utils.SocketCreator.*;

/**
 * Created by cheolho on 2017. 3. 27..
 */

public class MyApp extends Application {
    public static final String TAG = MyApp.class.getSimpleName();
    private Socket mSocket = null;
    private AppStatus mAppStatus = AppStatus.FOREGROUND;

    public void setSocket( Socket socket ) {
        if(socket != null) {
            Log.d(TAG, "setSocket: ");
            mSocket = socket;   
        }
            
    }
    public Socket getSocket() {
        if( mSocket != null ) {
            return mSocket;
        }
        return null;
    }
    public void connectSocket() {
        if( mSocket != null && !mSocket.connected()) {
            Log.d(TAG, "connectSocket: ");
            mSocket.connect();
        }
    }
    public void disconnectSocket() {
        if( mSocket != null && mSocket.connected()) {
            Log.d(TAG, "disconnectSocket: ");
            mSocket.disconnect();
        }
    }
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        Intent intent = new Intent(this, SocketService.class);
        intent.setAction(SocketTask.ACTION_SOCKET_CREATE);
        startService(intent);


    }
    public MyApp get(Context context) {
        return (MyApp) context.getApplicationContext();
    }

    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    // check if app is foreground
    public boolean isForeground() {
        return mAppStatus.ordinal() > AppStatus.BACKGROUND.ordinal();
    }

    public enum AppStatus {
        BACKGROUND,                // app is background
        RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
        FOREGROUND;                // app is foreground
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
                Log.d(TAG, "onActivityStarted: ");
                Intent intent = new Intent(activity, SocketService.class);
                intent.setAction(SocketTask.ACTION_SOCKET_CONNECT);
                startService(intent);
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
            } else if (running > 1) {
                // 2 or more running activities,
                // should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
            }

        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {

            if (--running == 0) {
                Log.d(TAG, "onActivityStopped: background");
                Intent intent = new Intent(activity, SocketService.class);
                intent.setAction(SocketTask.ACTION_SOCKET_DISCONNECT);
                startService(intent);
                mAppStatus = AppStatus.BACKGROUND;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
