package com.beakya.hellotalk.utils;

import android.content.Context;

import com.beakya.hellotalk.activity.PersonalChatActivity;
import com.beakya.hellotalk.event.Events;
import com.beakya.hellotalk.objs.ChatRoom;
import com.beakya.hellotalk.objs.GroupChatReadEventInfo;
import com.beakya.hellotalk.objs.PersonalChatReadEventInfo;

import org.greenrobot.eventbus.EventBus;
import com.beakya.hellotalk.utils.SocketEmitFunctions.*;
import java.util.ArrayList;

/**
 * Created by goodlife on 2017. 7. 22..
 */

public class SocketUtil {
    public static bFunction checkNotReadMessages(final PersonalChatReadEventInfo info, final Context context ) {
        return new SocketEmitFunctions.bFunction() {
            @Override
            public void apply() {
                ArrayList<String> notReadMessageList = MsgUtils.getNotReadMessages(context, info.getChatId());
                if ( notReadMessageList.size() > 0 ) {
                    if ( info.getChatType() == ChatRoom.PERSONAL_CHAT_TYPE ) {
                        MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), info.getReceiver(), notReadMessageList);
                    }
                }
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
            }
        };
    }
    public static bFunction checkNotReadMessages(final GroupChatReadEventInfo info, final Context context ) {
        return new SocketEmitFunctions.bFunction() {
            @Override
            public void apply() {
                ArrayList<String> notReadMessageList = MsgUtils.getNotReadMessages(context, info.getChatId());
                if ( notReadMessageList.size() > 0 ) {
                    if ( info.getChatType() == ChatRoom.GROUP_CHAT_TYPE ) {
                        MsgUtils.readAllMessage(context, info.getChatType(), info.getChatId(), notReadMessageList);
                    }
                }
                EventBus.getDefault().post(new Events.MessageEvent(PersonalChatActivity.EVENT_SOMEONE_READ_MESSAGE, null));
            }
        };
    }
}
