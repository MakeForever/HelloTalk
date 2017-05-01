package com.beakya.hellotalk.utils;

import android.util.Log;

import com.beakya.hellotalk.events.MessageEvent;
import com.beakya.hellotalk.events.UserInfoEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by cheolho on 2017. 4. 1..
 */

public class SocketCreator {
    private static final String TAG = SocketCreator.class.getSimpleName();
    private static final String IP = "http://192.168.0.102:8888";
    public static IO.Options getOptions(String token) {
        IO.Options options = new IO.Options();
        options.query = "token=" + token;
        return options;
    }
    public static Socket createSocket(String token) throws URISyntaxException {
        IO.Options options = getOptions(token);
        Socket socket;
        socket = IO.socket(IP, options);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: EVENT_CONNECT");
            }

        });
        socket.on("receive_user_info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: receive_user_info");
                JSONObject info = (JSONObject) args[0];
                String message = null;
                try {
                    message = info.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new UserInfoEvent(message, info));

            }
        });

        socket.on("search_friends_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: search_friends_result");
                JSONObject list = (JSONObject) args[0];
                String message = null;
                try {
                    message = list.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new MessageEvent<JSONObject>(message, list));
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: disconnected");
            }

        });
        return socket;
    }

}
