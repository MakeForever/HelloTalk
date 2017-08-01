package com.beakya.hellotalk.activity;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by goodlife on 2017. 6. 16..
 */

public class ChatActivity extends ToolBarActivity {
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    public static final String EVENT_INVITED_USER = "event_invited_user";
}
