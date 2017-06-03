package com.beakya.hellotalk.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.services.ChatService;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Manager;
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
        options.timeout = -1;
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

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: EVENT_CONNECT_ERROR" );
            }
        });
        socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: EVENT_RECONNECT" );
            }
        });
        socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: EVENT_RECONNECT_ERROR" );
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
                EventBus.getDefault().post(new Events.FriendFindEvent(message, list));
            }
        });
        // 처음 채팅이 왔을때 초기화해야 한다
        socket.on("invite_to_personal_chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Message message = null;
                PersonalChatRoom chatRoom = null;
                try {
                    User sender = Utils.extractUserFromJson(data.getJSONObject("from"));
                    message = Utils.extractMessageFromJson(data.getJSONObject("message"));
                    chatRoom = Utils.extractPersonalChatRoomFromJson(data.getJSONObject("chat_room"), sender);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("message", message);
                intent.putExtra("chatRoom", chatRoom);
                intent.setAction(ChatTask.ACTION_STORAGE_CHAT_DATA);
                context.startService(intent);
            }
        });

        socket.on("chat_read", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: chat_read");
                JSONObject object = (JSONObject) args[0];
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("object", object.toString());
                intent.setAction(ChatTask.ACTION_HANDLE_READ_CHAT);
                context.startService(intent);
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
