package com.beakya.hellotalk.services;

/**
 * Created by goodlife on 2017. 5. 17..
 */


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.beakya.hellotalk.activity.NotifyActivity;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.beakya.hellotalk.objs.User;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of stringMessages data stringMessages and notification stringMessages. Data stringMessages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data stringMessages are the type
        // traditionally used with GCM. Notification stringMessages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification stringMessages. The Firebase console always sends notification
        // stringMessages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM stringMessages here.
        // Not getting stringMessages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }
            Map<String, String> map = remoteMessage.getData();
            int chatType = Integer.parseInt(map.get("chat_type"));
            if ( chatType == 1 ) {
                String message = map.get("message");
                User user = new User(map.get("user_id"), map.get("user_name"), false);
                PersonalChatRoom chatRoom = new PersonalChatRoom(
                        map.get("chat_id"),
                        chatType,
                        true,
                        user
                );
//                sendPersonalNotification(chatRoom, getApplicationContext(), message);
                scheduleJob();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody(), getApplicationContext());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
//        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//        Job myJob = dispatcher.newJobBuilder()
//                .setService(MyJobService.class)
//                .setTag("my-job-tag")
//                .setTrigger(Trigger.executionWindow(0, 0))
//                .build();
//        dispatcher.schedule(myJob);
        Intent intent = new Intent(this, NotifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClassName("com.beakya.hellotalk", "com.beakya.hellotalk.activity.NotifyActivity");
//        startActivity(intent);
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private boolean handleNow( String chatId ) {
        ContentResolver resolver = getContentResolver();
        Cursor chatCursor = resolver.query(
                TalkContract.ChatRooms.CONTENT_URI,
                new String[] { TalkContract.ChatRooms.CHAT_ROOM_TYPE, TalkContract.ChatRooms.IS_SYNCHRONIZED },
                TalkContract.ChatRooms.CHAT_ID + " = ?", new String[] { chatId },
                null);


        return (chatCursor.getCount() > 0) ? true : false;
    }

    private void sendPersonalNotification(PersonalChatRoom chatRoom, Context context, String message) {
        Intent intent = new Intent(this, NotifyActivity.class);
        intent.putExtra("chatRoom", chatRoom);
        intent.putExtra("is_stored", handleNow(chatRoom.getChatId()));
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
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
//    private void sendNotification(User user, Context context) {
//        Log.d(TAG, "sendNotification: ");
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
//                .setSmallIcon(R.drawable.ic_menu_camera)
//
//                .setContentTitle(user.getName())
//                .setContentText()
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri);
//
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }
}
