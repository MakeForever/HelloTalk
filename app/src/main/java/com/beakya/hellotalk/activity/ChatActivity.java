package com.beakya.hellotalk.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by goodlife on 2017. 6. 16..
 */

public abstract class ChatActivity extends ToolBarActivity {
    public static final String EVENT_BUS_ACTION_INVITE_RESULT = "event_bus_action_invite_result";
    public static final String EVENT_NEW_MESSAGE_ARRIVED = "event_new_message_arrived";
    public static final String EVENT_SOMEONE_READ_MESSAGE = "event_someone_read_message";
    public static final String EVENT_INVITED_USER = "event_invited_user";
    public static final String EVENT_USER_CHANGE_PROFILE_IMG = "event_user_change_profile_img";
    private Button ImageOpenButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setToolbarContentView(int activityResId) {
        super.setToolbarContentView(activityResId);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public abstract String getCurrentChatId ();
}
