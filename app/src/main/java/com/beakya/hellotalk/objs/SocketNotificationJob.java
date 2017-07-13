package com.beakya.hellotalk.objs;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.MainActivity;
import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.utils.Serializers;
import com.beakya.hellotalk.utils.SocketManager;
import com.beakya.hellotalk.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by goodlife on 2017. 7. 10..
 */

public class SocketNotificationJob extends Job {
    String event;
    PayLoad<String> payLoad;
    Context context;
    public SocketNotificationJob ( String event, PayLoad<String> payLoad, Context context ) {
        this.event = event;
        this.payLoad = payLoad;
        this.context = context;
    }
    @Override
    public void run() {
        try {
            final Socket socket =  IO.socket(SocketManager.IP, Utils.getOptions(context));
            Emitter.Listener transferEndListener = new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    socket.off("send_Notification_data");
                    socket.off("end");
                    socket.disconnect();
                }
            };
            Emitter.Listener notificationDataListener = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JsonObject object = new JsonParser().parse( (String) args[0]).getAsJsonObject();
                    String event = object.get("event").getAsString();
                    switch ( event ) {
                        case "send_group_message" :
                            break;
                        case "invite_group_chat" :
                            break;
                        case "invite_to_personal_chat" :
                            sendPersonalNotification(object, context);
                            break;
                        default :

                    }
                }
            };
            socket.connect();
            socket.on("send_notification_data", notificationDataListener );
            socket.on("end", transferEndListener );
            socket.emit("send_notification_data", payLoad.getData());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
            Utils.ChatInitialize( context, chatRoom);
            if ( !(userQueryCursor.getCount() > 0) ) {
                Utils.insertUser(context, sender);
            }
        }
        Utils.insertMessage(context, message, chatRoom.getChatId(), false);


//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setStyle(new NotificationCompat.BigTextStyle())
//                .setSmallIcon(R.drawable.ic_menu_camera)
//                .setContentTitle(chatRoom.getTalkTo().getName())
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setPriority(Notification.PRIORITY_DEFAULT)
//                .setFullScreenIntent(pendingIntent, true);
        Intent intent = new Intent(context, PersonalChatActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, messageNotificationBuilder(sender, message, intent, context));
    }
    private Notification messageNotificationBuilder(User user, Message message, Intent intent, Context context ) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setLargeIcon(user.getProfileImg(context))
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(user.getName())
                .setContentText(message.getMessageContent())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);

        return notificationBuilder.build();
    }
}
