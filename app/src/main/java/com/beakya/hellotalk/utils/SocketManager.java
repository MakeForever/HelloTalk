package com.beakya.hellotalk.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.beakya.hellotalk.event.Events;
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

public class SocketManager {
    private final String TAG = SocketManager.class.getSimpleName();
    public static final String INVITE_TO_PERSONAL_CHAT = "invite_to_personal_chat";
    public static final String SEND_GROUP_CHAT_MESSAGE = "send_group_message";
    public static final String SOMEONE_CHAT_READ = "chat_read";
    public static final String INVITE_GROUP_CHAT ="invite_group_chat";
    public static final String INVITE_FRIEND = "invite_friend";
    public static final String RECEIVE_ALL_EVENT = "read_all_event";
    public static final String IP = "http://10.0.2.2:8888";

    private Context context;
    public SocketManager(Context context) {
        this.context = context;
    }

    public Socket createSocket() throws URISyntaxException {
        IO.Options options = Utils.getOptions(context);
        final Socket socket = IO.socket(IP, options);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                TaskRunner.setSocketState(true);
                TaskRunner.getInstance().execute();
                socket.emit("send_event_and_message");
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
        socket.on(INVITE_TO_PERSONAL_CHAT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String info = (String) args[0];
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra( "info", info );
                intent.setAction(ChatTask.ACTION_STORAGE_PERSONAL_CHAT_DATA);
                context.startService(intent);
            }
        });

        socket.on(SOMEONE_CHAT_READ, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: chat_read");
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_HANDLE_READ_CHAT);
                context.startService(intent);
            }
        });
        socket.on(SEND_GROUP_CHAT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_STORAGE_GROUP_CHAT_DATA);
                context.startService(intent);
            }
        });
        socket.on(INVITE_GROUP_CHAT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_STORAGE_GROUP_CHAT_INVITE);
                context.startService(intent);
                Log.d(TAG, "call: invite_group_chat" );
            }
        });
        socket.on(INVITE_FRIEND, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_INVITE_TO_GROUP_CHAT);
                context.startService(intent);
                Log.d(TAG, "call invite_friend:");
            }
        });
        socket.on("send_initial_state", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_READ_INITIAL_STATE);
                context.startService(intent);
                Log.d(TAG, "call RECEIVE_ALL_EVENT");
            }

        });
        socket.on("someone_leave_chat_room", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent(context, ChatService.class);
                intent.putExtra("info", (String) args[0]);
                intent.setAction(ChatTask.ACTION_SOMEONE_LEAVE_CHAT_ROOM);
                context.startService(intent);
                Log.d(TAG, "call: someone_leave_chat_room");
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                TaskRunner.setSocketState(false);
                Log.d(TAG, "call: disconnected");
            }
        });

        return socket;
    }

}
