package com.beakya.hellotalk.services;

/**
 * Created by goodlife on 2017. 5. 17..
 */


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import android.util.Log;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.GroupChatActivity;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.GroupChatRoom;
import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.utils.HashMapDeserializer;
import com.beakya.hellotalk.utils.Serializers;
import com.beakya.hellotalk.utils.SocketManager;
import com.beakya.hellotalk.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.R.id.message;
import static com.beakya.hellotalk.activity.PersonalChatActivity.EVENT_NEW_MESSAGE_ARRIVED;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String key = data.get("key");
        try {
            final Socket socket =  IO.socket(SocketManager.IP, Utils.getTemporaryOptions(getApplicationContext()));
//            final Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        this.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    if (socket.connected() ) {
//                        socket.off("send_Notification_data");
//                        socket.off("end");
//                        socket.disconnect();
//                    }
//                }
//            };
//            Thread thread =  new Thread(runnable);
//
            Emitter.Listener transferEndListener = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "call: transferEndListener");
                    socket.off("send_Notification_data");
                    socket.off("end");
                    socket.disconnect();
                }
            };
            Emitter.Listener notificationDataListener = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "call: " + (String) args[0]);
                    JsonObject object = new JsonParser().parse( (String) args[0]).getAsJsonObject();
                    String event = object.get("event").getAsString();
                    switch ( event ) {
                        case "send_group_message" :
                            handleGroupMessageNotification(object, getApplicationContext());
                            break;
                        case "invite_group_chat" :
                            handelGroupChatInvite(object, getApplicationContext());
                            break;
                        case "invite_to_personal_chat" :
                            sendPersonalNotification(object, getApplicationContext());
                            break;
                        default :

                    }
                }
            };
            socket.connect();
            socket.on("send_notification_data", notificationDataListener );
            socket.on("end", transferEndListener );
            socket.emit("send_notification_data", key);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onMessageReceived: " + key);
    }

    private void handleGroupMessageNotification( JsonObject object, Context context ) {
        JsonElement element = object.get("message");
        Message message = new Gson().fromJson(element, Message.class);
        GroupChatRoom chatRoom = Utils.getGroupChatRoom(message.getChatId(), context);
        Utils.insertMessage(context, message, message.getChatId(), false);
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.new_ic_launcher))
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(message.getCreatorId())
                .setContentText(message.getMessageContent())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
    private void sendPersonalNotification(JsonObject object, Context context ) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new Serializers.UserDeserializer())
                .create();


        JsonElement messageElement = object.get("message");
        JsonObject chatRoomElement = object.get("chatRoom").getAsJsonObject();
        JsonObject senderJsonObj = chatRoomElement.get("talkTo").getAsJsonObject();


        User sender = null;
        PersonalChatRoom chatRoom = null;
        Message message = null;
        try {
            sender = gson.fromJson(senderJsonObj, User.class);
            chatRoom = gson.fromJson(chatRoomElement, PersonalChatRoom.class);
            message = gson.fromJson(messageElement, Message.class);
        } catch ( JsonSyntaxException e ) {
            e.printStackTrace();
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor chatRoomQueryCursor = resolver.query(
                TalkContract.ChatRooms.CONTENT_URI,
                null,
                TalkContract.ChatRooms.CHAT_ID + "= ?",
                new String[] { chatRoom.getChatId() } ,
                null
        );
        Cursor userQueryCursor = resolver.query(
                TalkContract.User.CONTENT_URI,
                null,
                TalkContract.User.USER_ID + " = ? ",
                new String [] { sender.getId() },
                null
        );

        if( !(chatRoomQueryCursor.getCount() > 0) ) {
            Log.d(TAG, "task: ChatInitialize ");
            Utils.ChatInitialize( context, chatRoom);
            if ( !(userQueryCursor.getCount() > 0) ) {
                Utils.insertUser(context, sender);
            }
        }
        Utils.insertMessage(context, message, chatRoom.getChatId(), false);
        Intent intent = new Intent(context, PersonalChatActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, messageNotificationBuilder(sender, message, intent));
    }
    private Notification messageNotificationBuilder(User user, Message message, Intent intent ) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(user.getProfileImg(this))
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(user.getName())
                .setContentText(message.getMessageContent())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);

        return notificationBuilder.build();
    }
    private void handelGroupChatInvite ( JsonObject object, Context context ) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(HashMap.class, new HashMapDeserializer())
                .create();
        GroupChatRoom groupChatRoom = null;
        try {
            JsonElement element = object.get("chatRoom");
            groupChatRoom = gson.fromJson(element, GroupChatRoom.class);
        } catch ( JsonParseException e) {
            e.printStackTrace();
        }
        Utils.ChatInitialize(context, groupChatRoom);

        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("chatRoom", groupChatRoom);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.new_ic_launcher))
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle("Hello Talk")
                .setContentText(groupChatRoom.getChatName() + "에 초대되었습니다")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
