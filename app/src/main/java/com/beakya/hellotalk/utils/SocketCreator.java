package com.beakya.hellotalk.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beakya.hellotalk.events.MessageEvent;
import com.beakya.hellotalk.events.UserInfoEvent;
import com.beakya.hellotalk.services.ChatService;

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
    private final String TAG = SocketCreator.class.getSimpleName();
    private final String IP = "http://192.168.0.102:8888";
    private Context context;
    public SocketCreator(Context context) {
        this.context = context;
    }

    public IO.Options getOptions(String token) {
        IO.Options options = new IO.Options();
        options.query = "token=" + token;
        return options;
    }
    public Socket createSocket(String token) throws URISyntaxException {
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
        // 처음 채팅이 왔을때 초기화해야 한다
        socket.on("receive_chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("data", data.toString());
                intent.setAction(ChatTask.ACTION_STORAGE_CHAT_DATA);
                context.startService(intent);
            }
        });
        //내가 보낸 채팅의 결과값을 받을때
        socket.on("chat_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "socket invite_result call");
                JSONObject data = (JSONObject) args[0];
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("data", data.toString());
                intent.setAction(ChatTask.ACTION_CHAT_SEND_RESULT);
                context.startService(intent);
            }
        });
        socket.on("invite_user_to_room", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
        socket.on("leave_room", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
        socket.on("enter_room", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

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
