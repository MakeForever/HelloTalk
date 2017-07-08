package com.beakya.hellotalk.utils;

import android.content.Context;
import android.util.Log;

import com.beakya.hellotalk.MyApp;

import java.net.URISyntaxException;

import io.socket.client.Socket;

/**
 * Created by cheolho on 2017. 4. 6..
 */

public class SocketTask {

    private static final String TAG = SocketTask.class.getSimpleName();
    public static final String ACTION_SOCKET_CREATE = "socket_create";
    public static final String ACTION_SOCKET_CONNECT = "socket_connect";
    public static final String ACTION_SOCKET_DISCONNECT = "socket_disconnect";
    public static final String ACTION_SOCKET_CREATE_AND_EMIT ="socket_create_and_emit";
    public static void task(String action, Context context) {
        switch ( action ) {
            case ACTION_SOCKET_CONNECT :
                Log.d(TAG, "task: ACTION_SOCKET_CONNECT");
                connectSocket(context);
                break;
            case ACTION_SOCKET_DISCONNECT :
                Log.d(TAG, "task: ACTION_SOCKET_DISCONNECT");
                disconnectSocket(context);
                break;
            case ACTION_SOCKET_CREATE :
                Log.d(TAG, "task: ACTION_SOCKET_CREATE");
                createSocketTask(context, false);
                break;
            case ACTION_SOCKET_CREATE_AND_EMIT:
                Log.d(TAG, "task: ACTION_SOCKET_CREATE_AND_EMIT");
                createSocketTask(context, true);
                break;
            default :
                throw new RuntimeException("no matched action");
        }
    }


    private static void connectSocket(Context context) {
        if( Utils.checkToken(context) ) {
            MyApp app = Utils.getMyApp(context);
            app.connectSocket();
        }
    }
    private static void disconnectSocket(Context context) {
        MyApp app = Utils.getMyApp(context);
        app.disconnectSocket();

    }
    private static void createSocketTask(Context context, boolean flag) {
        if( Utils.checkToken(context) ) {
            try {
                Log.d(TAG, "createSocketTask: ");
                SocketManager manager = new SocketManager(context);
                manager.setSocketConnectionListener(SocketTaskRunner.getInstance().getListener());
                Socket socket = manager.createSocket();
                MyApp app = Utils.getMyApp(context);
                app.setSocket(socket);
                socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

}